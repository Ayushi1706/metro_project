package org.spring.metro.models.dto;

import java.time.LocalDateTime;

public record RouteDto(
        Long routeId,
        String routeName,
        Double totalDistance,
        Integer estimatedTime,
        String lineColor,
        Long startStationId,
        Long endStationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}