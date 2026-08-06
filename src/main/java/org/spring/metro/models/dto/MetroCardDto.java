package org.spring.metro.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MetroCardDto(
        Long cardId,
        Long passengerId,
        BigDecimal balance,
        LocalDate issueDate,
        LocalDate expiryDate,
        LocalDateTime createdAt,
        Boolean isActive
) {}