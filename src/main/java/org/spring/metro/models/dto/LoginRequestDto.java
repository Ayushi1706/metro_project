package org.spring.metro.models.dto;

public record LoginRequestDto(
        String contact,
        String password
) {}