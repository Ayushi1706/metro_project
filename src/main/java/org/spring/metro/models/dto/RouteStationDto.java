package org.spring.metro.models.dto;

public record RouteStationDto(
        Long routeId,
        Long stationId,
        Integer sequenceNumber
) {}