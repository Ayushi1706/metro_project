package org.spring.metro.models.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(
        String paymentId,
        String ticketId,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}