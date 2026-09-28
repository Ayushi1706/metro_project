package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.MaintenanceIssueDto;
import org.spring.metro.models.dto.MaintenanceIssueRequestDto;
import org.spring.metro.models.entity.MaintenanceIssue;
import org.spring.metro.models.entity.Station;
import org.spring.metro.models.entity.Train;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.IssueStatus;
import org.spring.metro.repository.MaintenanceIssueRepository;
import org.spring.metro.repository.StationRepository;
import org.spring.metro.repository.TrainRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceIssueService {

    private final MaintenanceIssueRepository maintenanceIssueRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public MaintenanceIssueDto createIssue(
            MaintenanceIssueRequestDto request) {

        Train train = trainRepository.findById(request.trainId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Train not found"
                        ));

        Station station = stationRepository.findById(request.stationId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Station not found"
                        ));

        User user = userRepository.findById(request.reportedBy())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reporting user not found"
                        ));

        MaintenanceIssue issue = MaintenanceIssue.builder()
                .issueId(UUID.randomUUID().toString())
                .train(train)
                .station(station)
                .reportedBy(user)
                .issueType(request.issueType())
                .description(request.description())
                .status(IssueStatus.OPEN)
                .priority(request.priority())
                .createdAt(LocalDateTime.now())
                .build();

        MaintenanceIssue savedIssue =
                maintenanceIssueRepository.save(issue);

        return toDto(savedIssue);
    }

    public MaintenanceIssueDto getIssueById(String issueId) {

        MaintenanceIssue issue =
                maintenanceIssueRepository.findById(issueId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Maintenance issue not found"
                                ));

        return toDto(issue);
    }

    public MaintenanceIssueDto updateStatus(
            String issueId,
            IssueStatus status) {

        MaintenanceIssue issue =
                maintenanceIssueRepository.findById(issueId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Maintenance issue not found"
                                ));

        issue.setStatus(status);

        if (status == IssueStatus.RESOLVED) {
            issue.setResolvedAt(LocalDateTime.now());
        } else {
            issue.setResolvedAt(null);
        }

        MaintenanceIssue updatedIssue =
                maintenanceIssueRepository.save(issue);

        return toDto(updatedIssue);
    }

    private MaintenanceIssueDto toDto(MaintenanceIssue issue) {

        return new MaintenanceIssueDto(
                issue.getIssueId(),
                issue.getTrain().getTrainId(),
                issue.getStation().getStationId(),
                issue.getReportedBy().getUserId().toString(),
                issue.getIssueType(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getCreatedAt(),
                issue.getResolvedAt()
        );
    }
}