package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Ticket;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.Role;
import org.spring.metro.repository.TicketRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Map<String, Object>>> getStaff(
            Pageable pageable) {

        List<Role> staffRoles = List.of(
                Role.ADMIN,
                Role.MAINTENANCE_STAFF
        );

        Page<Map<String, Object>> staff = userRepository
                .findByRoleIn(staffRoles, pageable)
                .map(this::toSafeUserResponse);

        return ResponseEntity.ok(staff);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id) {

        User user = findUserOrThrow(id);

        return ResponseEntity.ok(toSafeUserResponse(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String role,
            Pageable pageable) {

        Page<User> users;

        if (role != null && !role.isBlank()) {
            Role userRole;

            try {
                userRole = Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid role");
            }

            users = userRepository.findByRole(userRole, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return ResponseEntity.ok(users.map(this::toSafeUserResponse));
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody User request,
            Authentication authentication) {

        User user = findUserOrThrow(id);

        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwnAccount = authentication != null &&
                user.getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdmin && !isOwnAccount) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only update your own profile");
        }

        if (request.getFirstName() == null ||
                request.getFirstName().isBlank() ||
                request.getLastName() == null ||
                request.getLastName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "First name and last name are required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getUserId().equals(id))
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email is already in use");
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim());
        user.setContact(request.getContact());

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(toSafeUserResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        User user = findUserOrThrow(id);
        user.setIsActive(false);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    // Get user's tickets
    @GetMapping("/{id}/tickets")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getUserTickets(
            @PathVariable Long id,
            Authentication authentication) {

        User user = findUserOrThrow(id);

        boolean isAdminOrStaff = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN") ||
                                        authority.getAuthority().equals("ROLE_MAINTENANCE_STAFF"));

        boolean isOwnAccount = authentication != null &&
                user.getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdminOrStaff && !isOwnAccount) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view your own tickets");
        }

        List<Map<String, Object>> tickets = ticketRepository
                .findByPassenger_UserId(id)
                .stream()
                .map(this::toSafeTicketResponse)
                .toList();

        return ResponseEntity.ok(tickets);
    }


    @PatchMapping("/change-password")
    @Transactional
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null ||
                newPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password and new password are required");
        }

        if (newPassword.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must contain at least 8 characters");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }


    private Map<String, Object> toSafeUserResponse(User user) {
        return Map.of(
                "userId", user.getUserId(),
                "name", user.getFirstName() + " " + user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "contact", user.getContact() == null ? "" : user.getContact(),
                "registrationDate", user.getRegistrationDate(),
                "isActive", user.getIsActive()
        );
    }


    private Map<String, Object> toSafeTicketResponse(Ticket ticket) {
        return Map.of(
                "ticketId", ticket.getTicketId(),
                "passengerId", ticket.getPassenger().getUserId(),
                "fareId", ticket.getFare().getFareId(),
                "sourceStationId", ticket.getSourceStation().getStationId(),
                "destinationStationId", ticket.getDestStation().getStationId(),
                "ticketType", ticket.getTicketType(),
                "validUntil", ticket.getValidUntil(),
                "isUsed", ticket.getIsUsed(),
                "issueTime", ticket.getIssueTime(),
                "createdAt", ticket.getCreatedAt()
        );
    }
}

