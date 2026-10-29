package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.MetroCard;
import org.spring.metro.models.entity.User;
import org.spring.metro.repository.MetroCardRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/metrocards")
@RequiredArgsConstructor
public class MetroCardController {


    private final MetroCardRepository metroCardRepository;
    private final UserRepository userRepository;


    @PostMapping
    @Transactional
    public ResponseEntity<MetroCard> createCard(
            @RequestBody MetroCard request) {

        if (request.getUser() == null
                || request.getUser().getUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passenger ID is required");
        }

        BigDecimal initialBalance = request.getBalance() != null
                ? request.getBalance()
                : BigDecimal.ZERO;

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Initial balance cannot be negative");
        }

        User user = userRepository.findById(
                        request.getUser().getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passenger not found"));

        MetroCard card = new MetroCard();
        card.setUser(user);
        card.setBalance(initialBalance);
        card.setIssueDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setCreatedAt(LocalDateTime.now());
        card.setIsActive(true);

        MetroCard savedCard = metroCardRepository.save(card);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedCard);
    }


    @GetMapping("/{cardId}")
    @Transactional(readOnly = true)
    public ResponseEntity<MetroCard> getCardById(
            @PathVariable Long cardId) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Metro card not found"));

        return ResponseEntity.ok(card);
    }


    @PutMapping("/{cardId}/topup")
    @Transactional
    public ResponseEntity<MetroCard> topUpCard(
            @PathVariable Long cardId,
            @RequestBody MetroCard request) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Metro card not found"));

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Metro card is inactive");
        }

        BigDecimal amount = request.getBalance();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Top-up amount must be greater than zero");
        }

        card.setBalance(card.getBalance().add(amount));

        MetroCard updatedCard = metroCardRepository.save(card);

        return ResponseEntity.ok(updatedCard);
    }


    @PutMapping("/{cardId}/deactivate")
    @Transactional
    public ResponseEntity<MetroCard> deactivateCard(
            @PathVariable Long cardId) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Metro card not found"));

        card.setIsActive(false);

        MetroCard updatedCard = metroCardRepository.save(card);

        return ResponseEntity.ok(updatedCard);
    }


}
