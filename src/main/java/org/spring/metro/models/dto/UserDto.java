package org.spring.metro.models.dto;

import org.spring.metro.models.enums.Role;

import java.time.LocalDate;

public record UserDto(
        Long userId,
        String name,
        String email,
        Role role,
        String contact,
        LocalDate registrationDate,
        Boolean isActive
) {}