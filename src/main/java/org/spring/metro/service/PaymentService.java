package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.PaymentDto;
import org.spring.metro.models.dto.PaymentRequestDto;
import org.spring.metro.models.entity.Payment;
import org.spring.metro.models.entity.Ticket;
import org.spring.metro.repository.PaymentRepository;
import org.spring.metro.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    public PaymentDto processPayment(PaymentRequestDto request) {

        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ticket not found"
                        ));

        if (paymentRepository
                .findByTicket_TicketId(request.ticketId())
                .isPresent()) {

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

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .ticket(ticket)
                .paymentMethod(request.paymentMethod())
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return toDto(savedPayment);
    }

    public PaymentDto getPaymentById(String paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Payment not found"
                        ));

        return toDto(payment);
    }

    public PaymentDto getPaymentByTicketId(Long ticketId) {

        Payment payment = paymentRepository
                .findByTicket_TicketId(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Payment not found for this ticket"
                        ));

        return toDto(payment);
    }

    private PaymentDto toDto(Payment payment) {

        return new PaymentDto(
                payment.getPaymentId(),
                payment.getTicket().getTicketId().toString(),
                payment.getAmount(),
                payment.getCreatedAt()
        );
    }
}