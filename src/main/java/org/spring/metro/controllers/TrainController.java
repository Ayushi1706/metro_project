package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Train;
import org.spring.metro.service.TrainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @GetMapping
    public ResponseEntity<Page<Train>> getAllTrains(
            Pageable pageable) {

        return ResponseEntity.ok(
                trainService.getAllTrains(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Train> getTrainById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                trainService.getTrainById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Train> createTrain(
            @RequestBody Train train) {

        return ResponseEntity.ok(
                trainService.createTrain(train)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Train> updateTrain(
            @PathVariable String id,
            @RequestBody Train train) {

        return ResponseEntity.ok(
                trainService.updateTrain(id, train)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrain(
            @PathVariable String id) {

        trainService.deleteTrain(id);

        return ResponseEntity.noContent().build();
    }
}