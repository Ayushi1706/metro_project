package org.spring.metro.models.dto;

import java.time.LocalDate;

public record StationDto(
        Long stationId,
        String stationCode,
        Double latitude,
        Double longitude,
        String address,
        LocalDate openedDate,
        String lineColor,
        Boolean isActive
) {}