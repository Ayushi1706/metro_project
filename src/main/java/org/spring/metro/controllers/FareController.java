package org.spring.metro.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Fare;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.FareRepository;
import org.spring.metro.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
public class FareController {


    private final FareRepository fareRepository;
    private final StationRepository stationRepository;

    // Create a fare
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Fare> createFare(@RequestBody Fare request) {

        if (request.getFareId() == null || request.getFareId().isBlank()) {
            throw new IllegalArgumentException("fareId is required");
        }

        if (fareRepository.existsById(request.getFareId())) {
            throw new IllegalStateException(
                    "Fare already exists: " + request.getFareId());
        }

        Station source = findStation(
                request.getSourceStation() == null
                        ? null
                        : request.getSourceStation().getStationId());

        Station destination = findStation(
                request.getDestinationStation() == null
                        ? null
                        : request.getDestinationStation().getStationId());

        validate(source, destination, request.getBaseFare());

        if (fareRepository.existsBySourceStationAndDestinationStation(
                source, destination)) {
            throw new IllegalStateException(
                    "A fare already exists for this station pair");
        }

        request.setSourceStation(source);
        request.setDestinationStation(destination);

        Fare savedFare = fareRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedFare);
    }

    // Get all fares
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Fare>> getAllFares() {
        return ResponseEntity.ok(fareRepository.findAll());
    }

    // Get a fare by ID
    @GetMapping("/{fareId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Fare> getFare(@PathVariable String fareId) {
        return ResponseEntity.ok(findFare(fareId));
    }

    // Get a fare between two stations
    @GetMapping("/between")
    @Transactional(readOnly = true)
    public ResponseEntity<Fare> getFareBetween(
            @RequestParam String source,
            @RequestParam String destination) {

        Station sourceStation = findStation(source);
        Station destinationStation = findStation(destination);

        Fare fare = fareRepository
                .findBySourceStationAndDestinationStation(
                        sourceStation, destinationStation)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No fare defined for " + source
                                + " -> " + destination));

        return ResponseEntity.ok(fare);
    }

    // Update a fare
    @PutMapping("/{fareId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Fare> updateFare(
            @PathVariable String fareId,
            @RequestBody Fare request) {

        Fare existingFare = findFare(fareId);

        String sourceId = request.getSourceStation() == null
                ? null
                : request.getSourceStation().getStationId();

        String destinationId = request.getDestinationStation() == null
                ? null
                : request.getDestinationStation().getStationId();

        Station source = findStation(sourceId);
        Station destination = findStation(destinationId);

        validate(source, destination, request.getBaseFare());

        fareRepository
                .findBySourceStationAndDestinationStation(source, destination)
                .filter(existing -> !existing.getFareId().equals(fareId))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "A fare already exists for this station pair");
                });

        existingFare.setSourceStation(source);
        existingFare.setDestinationStation(destination);
        existingFare.setBaseFare(request.getBaseFare());

        return ResponseEntity.ok(fareRepository.save(existingFare));
    }

    // Delete a fare
    @DeleteMapping("/{fareId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Void> deleteFare(@PathVariable String fareId) {
        fareRepository.delete(findFare(fareId));
        return ResponseEntity.noContent().build();
    }

    // Find a fare
    private Fare findFare(String fareId) {
        return fareRepository.findById(fareId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Fare not found: " + fareId));
    }

    // Find a station
    private Station findStation(String stationId) {
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Station id is required");
        }

        return stationRepository.findById(stationId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Station not found: " + stationId));
    }

    // Validate fare details
    private void validate(
            Station source,
            Station destination,
            Double baseFare) {

        if (source.getStationId().equals(destination.getStationId())) {
            throw new IllegalArgumentException(
                    "Source and destination stations must be different");
        }

        if (baseFare == null || !Double.isFinite(baseFare) || baseFare < 0) {
            throw new IllegalArgumentException(
                    "baseFare must be zero or positive");
        }
    }

    // Handle not found errors
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            EntityNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    // Handle duplicate fare errors
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(
            IllegalStateException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }

    // Handle invalid requests
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }


}
