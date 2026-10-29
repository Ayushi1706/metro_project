package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationRepository stationRepository;


    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Station>> getAllStations(
            Pageable pageable) {

        return ResponseEntity.ok(
                stationRepository.findAll(pageable)
        );
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Station> getStationById(
            @PathVariable String id) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Station not found with ID: " + id
                ));

        return ResponseEntity.ok(station);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Station> createStation(
            @RequestBody Station station) {

        if (station.getStationId() == null ||
                station.getStationId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Station ID is required"
            );
        }

        if (station.getStationCode() == null ||
                station.getStationCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Station code is required"
            );
        }

        if (stationRepository.existsById(station.getStationId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Station ID already exists"
            );
        }

        if (station.getLatitude() == null ||
                station.getLongitude() == null ||
                station.getAddress() == null ||
                station.getAddress().isBlank() ||
                station.getLineColor() == null ||
                station.getLineColor().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude, longitude, address, and line color are required"
            );
        }

        if (station.getLatitude() < -90 ||
                station.getLatitude() > 90 ||
                station.getLongitude() < -180 ||
                station.getLongitude() > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid latitude or longitude"
            );
        }

        if (station.getIsActive() == null) {
            station.setIsActive(true);
        }

        if (stationRepository.existsByStationCode(station.getStationCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Station code already exists"
            );
        }

        Station savedStation = stationRepository.save(station);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedStation);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Station> updateStation(
            @PathVariable String id,
            @RequestBody Station request) {

        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Station not found with ID: " + id
                ));

        if (request.getStationCode() == null ||
                request.getStationCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Station code is required"
            );
        }

        if (request.getLatitude() == null ||
                request.getLongitude() == null ||
                request.getAddress() == null ||
                request.getAddress().isBlank() ||
                request.getLineColor() == null ||
                request.getLineColor().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude, longitude, address, and line color are required"
            );
        }

        if (request.getLatitude() < -90 ||
                request.getLatitude() > 90 ||
                request.getLongitude() < -180 ||
                request.getLongitude() > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid latitude or longitude"
            );
        }

        if (stationRepository.existsByStationCode(request.getStationCode())
                && !existingStation.getStationCode()
                .equals(request.getStationCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Station code already exists"
            );
        }

        existingStation.setStationCode(request.getStationCode());
        existingStation.setLatitude(request.getLatitude());
        existingStation.setLongitude(request.getLongitude());
        existingStation.setAddress(request.getAddress());
        existingStation.setOpenedDate(request.getOpenedDate());
        existingStation.setLineColor(request.getLineColor());

        if (request.getIsActive() != null) {
            existingStation.setIsActive(request.getIsActive());
        }

        Station updatedStation = stationRepository.save(existingStation);

        return ResponseEntity.ok(updatedStation);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Void> deleteStation(
            @PathVariable String id) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Station not found with ID: " + id
                ));

        stationRepository.delete(station);

        return ResponseEntity.noContent().build();
    }
}

