package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Schedule;
import org.spring.metro.repository.ScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Page<Schedule> getAllSchedules(Pageable pageable) {
        return scheduleRepository.findAll(pageable);
    }

    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found with id: " + id)
                );
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Long id, Schedule schedule) {

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found with id: " + id)
                );

        existingSchedule.setTrain(schedule.getTrain());
        existingSchedule.setRoute(schedule.getRoute());
        existingSchedule.setDayOfWeek(schedule.getDayOfWeek());
        existingSchedule.setScheduledDeparture(schedule.getScheduledDeparture());
        existingSchedule.setScheduledArrival(schedule.getScheduledArrival());
        existingSchedule.setValidFrom(schedule.getValidFrom());
        existingSchedule.setValidTo(schedule.getValidTo());
        existingSchedule.setIsActive(schedule.getIsActive());
        existingSchedule.setCreatedAt(schedule.getCreatedAt());

        return scheduleRepository.save(existingSchedule);
    }

    public void deleteSchedule(Long id) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found with id: " + id)
                );

        scheduleRepository.delete(schedule);
    }
}