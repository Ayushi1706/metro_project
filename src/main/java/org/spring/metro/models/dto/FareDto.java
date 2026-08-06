package org.spring.metro.models.dto;

import java.math.BigDecimal;

public record FareDto(
        String fareId,
        String sourceStationId,
        String destinationStationId,
        BigDecimal baseFare
) {
}