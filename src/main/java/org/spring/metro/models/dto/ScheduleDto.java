package org.spring.metro.models.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduleDto(
        String scheduleId,
        String trainId,
        String routeId,
        String dayOfWeek,
        LocalDateTime scheduledDeparture,
        LocalDateTime scheduledArrival,
        LocalDate validFrom,
        LocalDate validTo,
        boolean isActive
) {
}