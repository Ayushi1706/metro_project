package org.spring.metro.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.TicketBookingRequestDto;
import org.spring.metro.models.dto.TicketDto;
import org.spring.metro.models.entity.Fare;
import org.spring.metro.models.entity.Station;
import org.spring.metro.models.entity.Ticket;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.TicketType;
import org.spring.metro.repository.FareRepository;
import org.spring.metro.repository.StationRepository;
import org.spring.metro.repository.TicketRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final long VALIDITY_HOURS = 24;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final FareRepository fareRepository;

    @Transactional
    public TicketDto bookTicket(TicketBookingRequestDto request) {
        if (request.sourceStationId().equals(request.destinationStationId())) {
            throw new IllegalArgumentException("Source and destination stations must be different");
        }

        Long passengerId = parseId(request.passengerId());
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found: " + passengerId));

        Station source = stationRepository.findById(request.sourceStationId())
                .orElseThrow(() -> new EntityNotFoundException("Source station not found: " + request.sourceStationId()));
        Station dest = stationRepository.findById(request.destinationStationId())
                .orElseThrow(() -> new EntityNotFoundException("Destination station not found: " + request.destinationStationId()));

        Fare fare = fareRepository
                .findBySourceStationAndDestinationStation(source, dest)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No fare defined for " + source.getStationId() + " -> " + dest.getStationId()));

        TicketType ticketType;
        try {
            ticketType = TicketType.valueOf(request.ticketType().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid ticket type: " + request.ticketType());
        }

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setPassenger(passenger);
        ticket.setFare(fare);
        ticket.setSourceStation(source);
        ticket.setDestStation(dest);
        ticket.setTicketType(ticketType);
        ticket.setIssueTime(now);
        ticket.setValidUntil(now.plusHours(VALIDITY_HOURS));
        ticket.setIsUsed(false);
        ticket.setCreatedAt(now);

        return toDto(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public TicketDto getTicket(Long ticketId) {
        return toDto(findOrThrow(ticketId));
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByPassenger(Long passengerId) {
        return ticketRepository.findByPassenger_UserId(passengerId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public TicketDto useTicket(Long ticketId) {
        Ticket ticket = findOrThrow(ticketId);

        if (Boolean.TRUE.equals(ticket.getIsUsed())) {
            throw new IllegalStateException("Ticket already used");
        }
        if (ticket.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ticket has expired");
        }

        ticket.setIsUsed(true);
        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public void cancelTicket(Long ticketId) {
        Ticket ticket = findOrThrow(ticketId);
        if (Boolean.TRUE.equals(ticket.getIsUsed())) {
            throw new IllegalStateException("Used ticket cannot be cancelled");
        }
        ticketRepository.delete(ticket);
    }

    private Ticket findOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + ticketId));
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid passenger id: " + id);
        }
    }

    private TicketDto toDto(Ticket t) {
        return new TicketDto(
                t.getTicketId(),
                t.getPassenger().getUserId(),
                t.getFare().getFareId(),
                t.getSourceStation().getStationId(),
                t.getDestStation().getStationId(),
                t.getTicketType(),
                t.getValidUntil(),
                t.getIsUsed(),
                t.getIssueTime(),
                t.getCreatedAt()
        );
    }
}