package org.spring.metro.models.dto;

public record ChangePasswordDto(
        String oldPassword,
        String newPassword
) {
}
