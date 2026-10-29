package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Train;
import org.spring.metro.repository.TrainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@CrossOrigin
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainRepository trainRepository;

    // Get all trains
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Train>> getAllTrains(Pageable pageable) {
        return ResponseEntity.ok(trainRepository.findAll(pageable));
    }

    // Get train by ID
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Train> getTrainById(@PathVariable String id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Train not found with ID: " + id
                ));

        return ResponseEntity.ok(train);
    }

    // Create train
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Train> createTrain(@RequestBody Train request) {

        if (request.getTrainId() == null ||
                request.getTrainId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Train ID is required");
        }

        if (request.getTrainNumber() == null ||
                request.getTrainNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Train number is required");
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Capacity must be greater than zero");
        }

        if (request.getTotalCoaches() == null ||
                request.getTotalCoaches() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total coaches must be greater than zero");
        }

        if (request.getManufactureYear() == null ||
                request.getManufactureYear() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Manufacture year must be valid");
        }

        if (trainRepository.existsById(request.getTrainId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Train ID already exists");
        }

        if (trainRepository.existsByTrainNumber(request.getTrainNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Train number already exists");
        }

        request.setCreatedAt(LocalDateTime.now());

        if (request.getIsActive() == null) {
            request.setIsActive(true);
        }

        Train savedTrain = trainRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTrain);
    }

    // Update train
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Train> updateTrain(
            @PathVariable String id,
            @RequestBody Train request) {

        Train existingTrain = trainRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Train not found with ID: " + id
                ));

        if (request.getTrainNumber() == null ||
                request.getTrainNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Train number is required");
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Capacity must be greater than zero");
        }

        if (request.getTotalCoaches() == null ||
                request.getTotalCoaches() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total coaches must be greater than zero");
        }

        if (request.getManufactureYear() == null ||
                request.getManufactureYear() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Manufacture year must be valid");
        }

        if (trainRepository.existsByTrainNumber(request.getTrainNumber())
                && !existingTrain.getTrainNumber()
                .equals(request.getTrainNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Train number already exists");
        }

        existingTrain.setTrainNumber(request.getTrainNumber());
        existingTrain.setCapacity(request.getCapacity());
        existingTrain.setTotalCoaches(request.getTotalCoaches());
        existingTrain.setManufactureYear(request.getManufactureYear());

        if (request.getIsActive() != null) {
            existingTrain.setIsActive(request.getIsActive());
        }

        // Preserve the original createdAt value.
        Train updatedTrain = trainRepository.save(existingTrain);

        return ResponseEntity.ok(updatedTrain);
    }

    // Delete train
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteTrain(@PathVariable String id) {

        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Train not found with ID: " + id
                ));

        trainRepository.delete(train);

        return ResponseEntity.noContent().build();
    }
}

