package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.RouteDto;
import org.spring.metro.models.dto.RouteRequestDto;
import org.spring.metro.models.entity.Route;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.RouteRepository;
import org.spring.metro.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    public RouteDto createRoute(RouteRequestDto request) {

        Station startStation = stationRepository
                .findById(request.startStationId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Start station not found"
                        ));

        Station endStation = stationRepository
                .findById(request.endStationId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "End station not found"
                        ));

        Route route = new Route();

        route.setRouteName(request.routeName());
        route.setTotalDistance(request.totalDistance());
        route.setEstimatedTime(request.estimatedTime());
        route.setLineColor(request.lineColor());
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());

        Route savedRoute = routeRepository.save(route);

        return toDto(savedRoute);
    }

    public RouteDto getRouteById(Long routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Route not found"
                        ));

        return toDto(route);
    }

    public RouteDto updateRoute(
            Long routeId,
            RouteRequestDto request) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Route not found"
                        ));

        Station startStation = stationRepository
                .findById(request.startStationId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Start station not found"
                        ));

        Station endStation = stationRepository
                .findById(request.endStationId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "End station not found"
                        ));

        route.setRouteName(request.routeName());
        route.setTotalDistance(request.totalDistance());
        route.setEstimatedTime(request.estimatedTime());
        route.setLineColor(request.lineColor());
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setUpdatedAt(LocalDateTime.now());

        Route updatedRoute = routeRepository.save(route);

        return toDto(updatedRoute);
    }

    public void deleteRoute(Long routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Route not found"
                        ));

        routeRepository.delete(route);
    }

    private RouteDto toDto(Route route) {

        return new RouteDto(
                route.getRouteId(),
                route.getRouteName(),
                route.getTotalDistance(),
                route.getEstimatedTime(),
                route.getLineColor(),
                route.getStartStation().getStationId(),
                route.getEndStation().getStationId(),
                route.getCreatedAt(),
                route.getUpdatedAt()
        );
    }
}