package org.spring.metro.models.dto;

import org.spring.metro.models.enums.PaymentMethod;

public record PaymentRequestDto(
        Long ticketId,
        PaymentMethod paymentMethod
) {
}