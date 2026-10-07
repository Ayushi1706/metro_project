package org.spring.metro.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.TicketBookingRequestDto;
import org.spring.metro.models.dto.TicketDto;
import org.spring.metro.service.QrCodeService;
import org.spring.metro.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final QrCodeService qrCodeService;

    @PostMapping("/book")
    public ResponseEntity<TicketDto> bookTicket(@RequestBody TicketBookingRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.bookTicket(request));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDto> getTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicket(ticketId));
    }

    @GetMapping(value = "/{ticketId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTicketQr(@PathVariable Long ticketId) {
        TicketDto ticket = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(qrCodeService.generateTicketQr(ticket));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<TicketDto>> getTicketsByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(ticketService.getTicketsByPassenger(passengerId));
    }

    @PatchMapping("/{ticketId}/use")
    public ResponseEntity<TicketDto> useTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.useTicket(ticketId));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> cancelTicket(@PathVariable Long ticketId) {
        ticketService.cancelTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}