package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Payment;
import org.spring.metro.models.entity.Ticket;
import org.spring.metro.models.enums.PaymentMethod;
import org.spring.metro.repository.PaymentRepository;
import org.spring.metro.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    // Process a new payment
    @PostMapping
    @Transactional
    public ResponseEntity<Payment> processPayment(
            @RequestBody Payment request) {

        if (request.getTicket() == null ||
                request.getTicket().getTicketId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket ID is required"
            );
        }

        if (request.getPaymentMethod() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment method is required"
            );
        }

        Long ticketId = request.getTicket().getTicketId();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found"
                ));

        if (paymentRepository.findByTicket_TicketId(ticketId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment already exists for this ticket"
            );
        }

        if (ticket.getFare() == null ||
                ticket.getFare().getBaseFare() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fare information is not available for this ticket"
            );
        }

        BigDecimal amount =
                BigDecimal.valueOf(ticket.getFare().getBaseFare());

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setTicket(ticket);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(amount);
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedPayment);
    }

    // Get payment by payment ID
    @GetMapping("/{paymentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable String paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found"
                ));

        return ResponseEntity.ok(payment);
    }

    // Get payment by ticket ID
    @GetMapping("/ticket/{ticketId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Payment> getPaymentByTicketId(
            @PathVariable Long ticketId) {

        Payment payment = paymentRepository
                .findByTicket_TicketId(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found for this ticket"
                ));

        return ResponseEntity.ok(payment);
    }
}