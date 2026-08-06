package org.spring.metro.models.dto;

import java.time.LocalDate;

public record UserDto(
        Long userId,
        String firstName,
        String lastName,
        String role,
        String contact,
        LocalDate registrationDate,
        Boolean isActive,
        LocalDate hireDate,
        Long stationId
) {}