package org.spring.metro.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.FareDto;
import org.spring.metro.service.FareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
public class FareController {

    private final FareService fareService;

    @PostMapping
    public ResponseEntity<FareDto> createFare(@RequestBody FareDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fareService.createFare(dto));
    }

    @GetMapping
    public ResponseEntity<List<FareDto>> getAllFares() {
        return ResponseEntity.ok(fareService.getAllFares());
    }

    @GetMapping("/{fareId}")
    public ResponseEntity<FareDto> getFare(@PathVariable String fareId) {
        return ResponseEntity.ok(fareService.getFare(fareId));
    }

    @GetMapping("/between")
    public ResponseEntity<FareDto> getFareBetween(@RequestParam String source,
                                                  @RequestParam String destination) {
        return ResponseEntity.ok(fareService.getFareBetween(source, destination));
    }

    @PutMapping("/{fareId}")
    public ResponseEntity<FareDto> updateFare(@PathVariable String fareId, @RequestBody FareDto dto) {
        return ResponseEntity.ok(fareService.updateFare(fareId, dto));
    }

    @DeleteMapping("/{fareId}")
    public ResponseEntity<Void> deleteFare(@PathVariable String fareId) {
        fareService.deleteFare(fareId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}