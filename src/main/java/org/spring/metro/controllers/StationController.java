package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Station;
import org.spring.metro.service.StationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<Page<Station>> getAllStations(
            Pageable pageable) {

        return ResponseEntity.ok(
                stationService.getAllStations(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                stationService.getStationById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Station> createStation(
            @RequestBody Station station) {

        return ResponseEntity.ok(
                stationService.createStation(station)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Station> updateStation(
            @PathVariable String id,
            @RequestBody Station station) {

        return ResponseEntity.ok(
                stationService.updateStation(id, station)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStation(
            @PathVariable String id) {

        stationService.deleteStation(id);

        return ResponseEntity.noContent().build();
    }
}