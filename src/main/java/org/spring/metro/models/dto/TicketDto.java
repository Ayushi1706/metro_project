package org.spring.metro.models.dto;

import org.spring.metro.models.enums.TicketType;

import java.time.LocalDateTime;

public record TicketDto(
        Long ticketId,
        Long passengerId,
        Long fareId,
        Long sourceStationId,
        Long destStationId,
        TicketType ticketType,
        LocalDateTime validUntil,
        Boolean isUsed,
        LocalDateTime issueTime,
        LocalDateTime createdAt
) {}