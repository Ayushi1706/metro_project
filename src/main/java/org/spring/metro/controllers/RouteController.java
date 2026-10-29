package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Route;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.RouteRepository;
import org.spring.metro.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    // Create a new route
    @PostMapping
    @Transactional
    public ResponseEntity<Route> createRoute(
            @RequestBody Route request) {

        if (request.getRouteName() == null ||
                request.getRouteName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route name is required"
            );
        }

        if (request.getTotalDistance() == null ||
                request.getTotalDistance() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total distance must be greater than zero"
            );
        }

        if (request.getEstimatedTime() == null ||
                request.getEstimatedTime() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estimated time must be greater than zero"
            );
        }

        if (request.getStartStation() == null ||
                request.getStartStation().getStationId() == null ||
                request.getEndStation() == null ||
                request.getEndStation().getStationId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start station ID and end station ID are required"
            );
        }

        String startStationId =
                request.getStartStation().getStationId();

        String endStationId =
                request.getEndStation().getStationId();

        if (startStationId.equals(endStationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start station and end station must be different"
            );
        }

        Station startStation = stationRepository
                .findById(startStationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Start station not found"
                ));

        Station endStation = stationRepository
                .findById(endStationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "End station not found"
                ));

        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setTotalDistance(request.getTotalDistance());
        route.setEstimatedTime(request.getEstimatedTime());
        route.setLineColor(request.getLineColor());
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());

        Route savedRoute = routeRepository.save(route);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedRoute);
    }

    // Get a route by ID
    @GetMapping("/{routeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Route> getRouteById(
            @PathVariable Long routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        return ResponseEntity.ok(route);
    }

    // Update an existing route
    @PutMapping("/{routeId}")
    @Transactional
    public ResponseEntity<Route> updateRoute(
            @PathVariable Long routeId,
            @RequestBody Route request) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        if (request.getRouteName() == null ||
                request.getRouteName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route name is required"
            );
        }

        if (request.getTotalDistance() == null ||
                request.getTotalDistance() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total distance must be greater than zero"
            );
        }

        if (request.getEstimatedTime() == null ||
                request.getEstimatedTime() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estimated time must be greater than zero"
            );
        }

        if (request.getStartStation() == null ||
                request.getStartStation().getStationId() == null ||
                request.getEndStation() == null ||
                request.getEndStation().getStationId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start station ID and end station ID are required"
            );
        }

        String startStationId =
                request.getStartStation().getStationId();

        String endStationId =
                request.getEndStation().getStationId();

        if (startStationId.equals(endStationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start station and end station must be different"
            );
        }

        Station startStation = stationRepository
                .findById(startStationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Start station not found"
                ));

        Station endStation = stationRepository
                .findById(endStationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "End station not found"
                ));

        route.setRouteName(request.getRouteName());
        route.setTotalDistance(request.getTotalDistance());
        route.setEstimatedTime(request.getEstimatedTime());
        route.setLineColor(request.getLineColor());
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setUpdatedAt(LocalDateTime.now());

        Route updatedRoute = routeRepository.save(route);

        return ResponseEntity.ok(updatedRoute);
    }

    // Delete a route
    @DeleteMapping("/{routeId}")
    @Transactional
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        routeRepository.delete(route);

        return ResponseEntity.noContent().build();
    }
}

