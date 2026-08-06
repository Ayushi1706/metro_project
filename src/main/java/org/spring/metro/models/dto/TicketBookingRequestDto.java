package org.spring.metro.models.dto;

public record TicketBookingRequestDto(
        String passengerId,
        String sourceStationId,
        String destinationStationId,
        String ticketType
) {
}