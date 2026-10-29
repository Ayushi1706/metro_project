package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Schedule;
import org.spring.metro.models.entity.Train;
import org.spring.metro.models.entity.Route;
import org.spring.metro.repository.ScheduleRepository;
import org.spring.metro.repository.TrainRepository;
import org.spring.metro.repository.RouteRepository;
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
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;


    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Schedule>> getAllSchedules(
            Pageable pageable) {

        return ResponseEntity.ok(
                scheduleRepository.findAll(pageable)
        );
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Schedule> getScheduleById(
            @PathVariable Long id) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + id
                ));

        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Schedule> createSchedule(
            @RequestBody Schedule request) {

        if (request.getTrain() == null ||
                request.getTrain().getTrainId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Train ID is required"
            );
        }

        if (request.getRoute() == null ||
                request.getRoute().getRouteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route ID is required"
            );
        }

        if (request.getDayOfWeek() == null ||
                request.getScheduledDeparture() == null ||
                request.getScheduledArrival() == null ||
                request.getValidFrom() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Day, departure time, arrival time, and valid-from date are required"
            );
        }

        if (!request.getScheduledArrival()
                .isAfter(request.getScheduledDeparture())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Arrival time must be later than departure time"
            );
        }

        if (request.getValidTo() != null &&
                request.getValidTo().isBefore(request.getValidFrom())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid-to date cannot be before valid-from date"
            );
        }

        Train train = trainRepository.findById(
                request.getTrain().getTrainId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Train not found"
        ));

        Route route = routeRepository.findById(
                request.getRoute().getRouteId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Route not found"
        ));

        Schedule schedule = new Schedule();
        schedule.setTrain(train);
        schedule.setRoute(route);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setScheduledDeparture(request.getScheduledDeparture());
        schedule.setScheduledArrival(request.getScheduledArrival());
        schedule.setValidFrom(request.getValidFrom());
        schedule.setValidTo(request.getValidTo());
        schedule.setIsActive(
                request.getIsActive() != null
                        ? request.getIsActive()
                        : true
        );
        schedule.setCreatedAt(LocalDateTime.now());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedSchedule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable Long id,
            @RequestBody Schedule request) {

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + id
                ));

        if (request.getTrain() == null ||
                request.getTrain().getTrainId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Train ID is required"
            );
        }

        if (request.getRoute() == null ||
                request.getRoute().getRouteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route ID is required"
            );
        }

        if (request.getDayOfWeek() == null ||
                request.getScheduledDeparture() == null ||
                request.getScheduledArrival() == null ||
                request.getValidFrom() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Day, departure time, arrival time, and valid-from date are required"
            );
        }

        if (!request.getScheduledArrival()
                .isAfter(request.getScheduledDeparture())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Arrival time must be later than departure time"
            );
        }

        if (request.getValidTo() != null &&
                request.getValidTo().isBefore(request.getValidFrom())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid-to date cannot be before valid-from date"
            );
        }

        Train train = trainRepository.findById(
                request.getTrain().getTrainId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Train not found"
        ));

        Route route = routeRepository.findById(
                request.getRoute().getRouteId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Route not found"
        ));

        existingSchedule.setTrain(train);
        existingSchedule.setRoute(route);
        existingSchedule.setDayOfWeek(request.getDayOfWeek());
        existingSchedule.setScheduledDeparture(request.getScheduledDeparture());
        existingSchedule.setScheduledArrival(request.getScheduledArrival());
        existingSchedule.setValidFrom(request.getValidFrom());
        existingSchedule.setValidTo(request.getValidTo());

        if (request.getIsActive() != null) {
            existingSchedule.setIsActive(request.getIsActive());
        }

        // Keep the original createdAt value during updates.
        Schedule updatedSchedule =
                scheduleRepository.save(existingSchedule);

        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long id) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + id
                ));

        scheduleRepository.delete(schedule);

        return ResponseEntity.noContent().build();
    }
}

