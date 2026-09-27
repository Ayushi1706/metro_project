package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.MetroCardCreateRequestDto;
import org.spring.metro.models.dto.MetroCardDto;
import org.spring.metro.models.dto.MetroCardTopUpRequestDto;
import org.spring.metro.service.MetroCardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrocards")
@RequiredArgsConstructor
public class MetroCardController {

    private final MetroCardService metroCardService;

    @PostMapping
    public ResponseEntity<MetroCardDto> createCard(
            @RequestBody MetroCardCreateRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        metroCardService.createCard(
                                request.passengerId(),
                                request.initialBalance()
                        )
                );
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<MetroCardDto> getCardById(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(
                metroCardService.getCardById(cardId)
        );
    }

    @PutMapping("/{cardId}/topup")
    public ResponseEntity<MetroCardDto> topUpCard(
            @PathVariable Long cardId,
            @RequestBody MetroCardTopUpRequestDto request) {

        return ResponseEntity.ok(
                metroCardService.updateBalance(
                        cardId,
                        request.amount()
                )
        );
    }

    @PutMapping("/{cardId}/deactivate")
    public ResponseEntity<MetroCardDto> deactivateCard(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(
                metroCardService.deactivateCard(cardId)
        );
    }
}