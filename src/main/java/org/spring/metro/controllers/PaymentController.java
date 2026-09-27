package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.PaymentDto;
import org.spring.metro.models.dto.PaymentRequestDto;
import org.spring.metro.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> processPayment(
            @RequestBody PaymentRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<PaymentDto> getPaymentByTicketId(
            @PathVariable Long ticketId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByTicketId(ticketId)
        );
    }
}