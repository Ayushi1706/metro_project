package org.spring.metro.models.dto;

public record RouteRequestDto(
        String routeName,
        Double totalDistance,
        Integer estimatedTime,
        String lineColor,
        String startStationId,
        String endStationId
) {}