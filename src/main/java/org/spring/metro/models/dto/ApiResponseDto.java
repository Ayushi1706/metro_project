package org.spring.metro.models.dto;

public record ApiResponseDto(
        boolean success,
        String message,
        Object data
) {
}