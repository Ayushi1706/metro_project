package org.spring.metro.models.dto;

import java.time.LocalDateTime;

public record TrainDto(
        Long trainId,
        String trainNumber,
        Integer capacity,
        Integer totalCoaches,
        Integer manufactureYear,
        LocalDateTime createdAt,
        Boolean isActive
) {}