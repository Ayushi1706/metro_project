package org.spring.metro.service;

import org.spring.metro.models.dto.TicketDto;
import org.spring.metro.models.dto.UserDto;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.Role;
import org.spring.metro.repository.TicketRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketRepository ticketRepository;

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        return toDto(user);
    }

    public Page<UserDto> getAllUsers(String role, Pageable pageable) {

        Page<User> users;

        if (role != null && !role.isBlank()) {
            try {
                Role userRole = Role.valueOf(role.toUpperCase());
                users = userRepository.findByRole(userRole, pageable);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid role"
                );
            }
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::toDto);
    }

    public UserDto updateUser(Long id, UserDto userDto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        String[] name = userDto.name()
                .trim()
                .split("\\s+", 2);

        user.setFirstName(name[0]);

        if (name.length > 1) {
            user.setLastName(name[1]);
        }

        user.setEmail(userDto.email());
        user.setContact(userDto.contact());

        user = userRepository.save(user);

        return toDto(user);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        user.setIsActive(false);

        userRepository.save(user);
    }

    public void changePassword(String oldPassword, String newPassword, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        if (!passwordEncoder.matches(
                oldPassword,
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password is incorrect"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }

    public Page<UserDto> getStaff(Pageable pageable) {

        List<Role> staffRoles = List.of(
                Role.ADMIN,
                Role.MAINTENANCE_STAFF
        );

        return userRepository
                .findByRoleIn(staffRoles, pageable)
                .map(this::toDto);
    }

    private UserDto toDto(User user) {

        return new UserDto(
                user.getUserId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getContact(),
                user.getRegistrationDate(),
                user.getIsActive()
        );
    }

    public List<TicketDto> getUserTickets(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return ticketRepository.findByPassenger_UserId(userId)
                .stream()
                .map(ticket -> new TicketDto(
                        ticket.getTicketId(),
                        ticket.getPassenger().getUserId(),
                        ticket.getFare().getFareId(),
                        ticket.getSourceStation().getStationId(),
                        ticket.getDestStation().getStationId(),
                        ticket.getTicketType(),
                        ticket.getValidUntil(),
                        ticket.getIsUsed(),
                        ticket.getIssueTime(),
                        ticket.getCreatedAt()
                ))
                .toList();
    }
}