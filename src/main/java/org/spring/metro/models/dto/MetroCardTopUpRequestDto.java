package org.spring.metro.models.dto;

import java.math.BigDecimal;

public record MetroCardTopUpRequestDto(
        BigDecimal amount
) {}