package org.spring.metro.models.dto;

public record LoginRequestDto(
        String email,
        String password
) {}