package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.ChangePasswordDto;
import org.spring.metro.models.dto.UserDto;
import org.spring.metro.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<Page<UserDto>> getStaff(
            @RequestParam(required = false) Long stationId,
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getStaff(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String role,
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getAllUsers(role, pageable)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {

        return ResponseEntity.ok(
                userService.updateUser(id, userDto)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tickets")

    public ResponseEntity<?> getUserTickets(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserTickets(id)
        );
    }

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordDto request,
            Authentication authentication) {

        userService.changePassword(
                request.oldPassword(),
                request.newPassword(),
                authentication.getName()
        );

        return ResponseEntity.ok("Password changed successfully");
    }
}