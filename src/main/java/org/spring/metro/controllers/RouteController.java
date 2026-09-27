package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.RouteDto;
import org.spring.metro.models.dto.RouteRequestDto;
import org.spring.metro.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteDto> createRoute(
            @RequestBody RouteRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(routeService.createRoute(request));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteDto> getRouteById(
            @PathVariable Long routeId) {

        return ResponseEntity.ok(
                routeService.getRouteById(routeId)
        );
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<RouteDto> updateRoute(
            @PathVariable Long routeId,
            @RequestBody RouteRequestDto request) {

        return ResponseEntity.ok(
                routeService.updateRoute(routeId, request)
        );
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long routeId) {

        routeService.deleteRoute(routeId);

        return ResponseEntity.noContent().build();
    }
}