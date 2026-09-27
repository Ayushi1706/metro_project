package org.spring.metro.models.dto;

import java.math.BigDecimal;

public record MetroCardCreateRequestDto(
        Long passengerId,
        BigDecimal initialBalance
) {}