package org.spring.metro.models.dto;

import java.math.BigDecimal;

public record TopUpRequestDto(
        Long cardId,
        BigDecimal amount
) {}