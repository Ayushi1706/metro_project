package org.spring.metro.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Fare;
import org.spring.metro.models.entity.Station;
import org.spring.metro.models.entity.Ticket;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.TicketType;
import org.spring.metro.repository.FareRepository;
import org.spring.metro.repository.StationRepository;
import org.spring.metro.repository.TicketRepository;
import org.spring.metro.repository.UserRepository;
import org.spring.metro.service.QrCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private static final long VALIDITY_HOURS = 24;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final FareRepository fareRepository;
    private final QrCodeService qrCodeService;

    // Book a ticket
    @PostMapping("/book")
    @Transactional
    public ResponseEntity<Ticket> bookTicket(
            @RequestBody Ticket request) {

        if (request.getPassenger() == null ||
                request.getPassenger().getUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passenger ID is required"
            );
        }

        if (request.getSourceStation() == null ||
                request.getSourceStation().getStationId() == null ||
                request.getDestStation() == null ||
                request.getDestStation().getStationId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source and destination station IDs are required"
            );
        }

        String sourceId = request.getSourceStation().getStationId();
        String destinationId = request.getDestStation().getStationId();

        if (sourceId.equals(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source and destination stations must be different"
            );
        }

        if (request.getTicketType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket type is required"
            );
        }

        User passenger = userRepository.findById(
                request.getPassenger().getUserId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Passenger not found"
        ));

        Station source = stationRepository.findById(sourceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Source station not found"
                ));

        Station destination = stationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destination station not found"
                ));

        Fare fare = fareRepository
                .findBySourceStationAndDestinationStation(source, destination)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No fare defined for the selected stations"
                ));

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setPassenger(passenger);
        ticket.setSourceStation(source);
        ticket.setDestStation(destination);
        ticket.setFare(fare);
        ticket.setTicketType(request.getTicketType());
        ticket.setIssueTime(now);
        ticket.setValidUntil(now.plusHours(VALIDITY_HOURS));
        ticket.setIsUsed(false);
        ticket.setCreatedAt(now);

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedTicket);
    }

    // Get a ticket by ID
    @GetMapping("/{ticketId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Ticket> getTicket(
            @PathVariable Long ticketId) {

        Ticket ticket = findTicketOrThrow(ticketId);
        return ResponseEntity.ok(ticket);
    }

    // Generate a ticket QR code
    @GetMapping(
            value = "/{ticketId}/qr",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getTicketQr(
            @PathVariable Long ticketId) {

        Ticket ticket = findTicketOrThrow(ticketId);

        byte[] qrImage = qrCodeService.generateTicketQr(ticket);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    // Get tickets belonging to a passenger
    @GetMapping("/passenger/{passengerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Ticket>> getTicketsByPassenger(
            @PathVariable Long passengerId) {

        List<Ticket> tickets =
                ticketRepository.findByPassenger_UserId(passengerId);

        return ResponseEntity.ok(tickets);
    }

    // Mark a ticket as used
    @PatchMapping("/{ticketId}/use")
    @Transactional
    public ResponseEntity<Ticket> useTicket(
            @PathVariable Long ticketId) {

        Ticket ticket = findTicketOrThrow(ticketId);

        if (Boolean.TRUE.equals(ticket.getIsUsed())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket has already been used"
            );
        }

        if (ticket.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket has expired"
            );
        }

        ticket.setIsUsed(true);

        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    // Cancel a ticket
    @DeleteMapping("/{ticketId}")
    @Transactional
    public ResponseEntity<Void> cancelTicket(
            @PathVariable Long ticketId) {

        Ticket ticket = findTicketOrThrow(ticketId);

        if (Boolean.TRUE.equals(ticket.getIsUsed())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A used ticket cannot be cancelled"
            );
        }

        ticketRepository.delete(ticket);

        return ResponseEntity.noContent().build();
    }

    private Ticket findTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found: " + ticketId
                ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            EntityNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(
            RuntimeException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}

