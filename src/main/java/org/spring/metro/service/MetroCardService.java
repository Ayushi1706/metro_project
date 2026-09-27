package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.MetroCardDto;
import org.spring.metro.models.entity.MetroCard;
import org.spring.metro.models.entity.User;
import org.spring.metro.repository.MetroCardRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetroCardService {

    private final MetroCardRepository metroCardRepository;
    private final UserRepository userRepository;

    public MetroCardDto createCard(Long passengerId, BigDecimal initialBalance) {

        User user = userRepository.findById(passengerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Passenger not found"
                        ));

        MetroCard card = new MetroCard();

        card.setUser(user);
        card.setBalance(initialBalance != null
                ? initialBalance
                : BigDecimal.ZERO);

        card.setIssueDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setCreatedAt(LocalDateTime.now());
        card.setIsActive(true);

        MetroCard savedCard = metroCardRepository.save(card);

        return toDto(savedCard);
    }

    public MetroCardDto getCardById(Long cardId) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Metro card not found"
                        ));

        return toDto(card);
    }

    public MetroCardDto updateBalance(
            Long cardId,
            BigDecimal amount) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Metro card not found"
                        ));

        if (!card.getIsActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Metro card is inactive"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero"
            );
        }

        card.setBalance(card.getBalance().add(amount));

        MetroCard updatedCard = metroCardRepository.save(card);

        return toDto(updatedCard);
    }

    public MetroCardDto deactivateCard(Long cardId) {

        MetroCard card = metroCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Metro card not found"
                        ));

        card.setIsActive(false);

        MetroCard updatedCard = metroCardRepository.save(card);

        return toDto(updatedCard);
    }

    private MetroCardDto toDto(MetroCard card) {

        return new MetroCardDto(
                card.getCardId(),
                card.getUser().getUserId(),
                card.getBalance(),
                card.getIssueDate(),
                card.getExpiryDate(),
                card.getCreatedAt(),
                card.getIsActive()
        );
    }
}