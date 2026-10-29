package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.MaintenanceIssue;
import org.spring.metro.models.entity.Station;
import org.spring.metro.models.entity.Train;
import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.IssuePriority;
import org.spring.metro.models.enums.IssueStatus;
import org.spring.metro.repository.MaintenanceIssueRepository;
import org.spring.metro.repository.StationRepository;
import org.spring.metro.repository.TrainRepository;
import org.spring.metro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance-issues")
@RequiredArgsConstructor
public class MaintenanceIssueController {

    private final MaintenanceIssueRepository issueRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;



    private MaintenanceIssue findIssue(String issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Maintenance issue not found: " + issueId));
    }

    private <T> T orNotFound(Optional<T> opt, String what, Object id) {
        return opt.orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, what + " not found: " + id));
    }

    private void applyStatus(MaintenanceIssue issue, IssueStatus newStatus) {
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Issue is already closed");
        }

        issue.setStatus(newStatus);

        if (newStatus == IssueStatus.RESOLVED ||
                newStatus == IssueStatus.CLOSED) {
            if (issue.getResolvedAt() == null) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        } else {
            issue.setResolvedAt(null);
        }
    }


    @PostMapping
    public ResponseEntity<MaintenanceIssue> createIssue(
            @RequestParam String trainId,
            @RequestParam String stationId,
            @RequestParam String issueType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "MEDIUM") String priority,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Logged-in user not found"));

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Train not found: " + trainId));

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Station not found: " + stationId));

        MaintenanceIssue issue = new MaintenanceIssue();
        issue.setTrain(train);
        issue.setStation(station);
        issue.setReportedBy(user);
        issue.setIssueType(issueType);
        issue.setDescription(description);
        issue.setPriority(IssuePriority.valueOf(priority.toUpperCase()));

        MaintenanceIssue savedIssue = issueRepository.save(issue);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedIssue);
    }



    @GetMapping("/{issueId}")
    public ResponseEntity<MaintenanceIssue> getIssueById(
            @PathVariable String issueId) {
        return ResponseEntity.ok(findIssue(issueId));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceIssue>> getAllIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) String trainId,
            @RequestParam(required = false) String stationId) {

        List<MaintenanceIssue> issues;

        if (trainId != null) {
            Train train = orNotFound(
                    trainRepository.findById(trainId), "Train", trainId);
            issues = issueRepository.findByTrain(train);
        } else if (stationId != null) {
            Station station = orNotFound(
                    stationRepository.findById(stationId), "Station", stationId);
            issues = issueRepository.findByStation(station);
        } else if (status != null && priority != null) {
            issues = issueRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            issues = issueRepository.findByStatus(status);
        } else if (priority != null) {
            issues = issueRepository.findByPriority(priority);
        } else {
            issues = issueRepository.findAll();
        }

        if (status != null) {
            issues = issues.stream()
                    .filter(i -> i.getStatus() == status).toList();
        }

        if (priority != null) {
            issues = issues.stream()
                    .filter(i -> i.getPriority() == priority).toList();
        }

        return ResponseEntity.ok(issues);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceIssue>> getOverdueIssues(
            @RequestParam(defaultValue = "24") long hours) {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);

        List<MaintenanceIssue> overdue = new ArrayList<>(
                issueRepository.findByStatusNotAndCreatedAtBefore(
                        IssueStatus.RESOLVED, cutoff));

        overdue.removeIf(i ->
                i.getStatus() == IssueStatus.CLOSED ||
                        i.getStatus() == IssueStatus.RESOLVED);

        return ResponseEntity.ok(overdue);
    }

    @GetMapping("/critical")
    public ResponseEntity<List<MaintenanceIssue>> getCriticalIssues() {
        List<MaintenanceIssue> critical =
                issueRepository.findByPriority(IssuePriority.CRITICAL)
                        .stream()
                        .filter(i -> i.getStatus() == IssueStatus.OPEN ||
                                i.getStatus() == IssueStatus.IN_PROGRESS)
                        .sorted(Comparator.comparing(
                                MaintenanceIssue::getCreatedAt).reversed())
                        .toList();

        return ResponseEntity.ok(critical);
    }

    @GetMapping("/between")
    public ResponseEntity<List<MaintenanceIssue>> getIssuesBetween(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {

        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' must be before 'to'");
        }

        return ResponseEntity.ok(
                issueRepository.findByCreatedAtBetween(from, to));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (IssueStatus s : IssueStatus.values()) {
            byStatus.put(s.name(), issueRepository.countByStatus(s));
        }

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (IssuePriority p : IssuePriority.values()) {
            byPriority.put(p.name(), issueRepository.countByPriority(p));
        }

        OptionalDouble avgResolutionMinutes = issueRepository.findAll()
                .stream()
                .filter(i -> i.getResolvedAt() != null)
                .mapToLong(i -> java.time.Duration.between(
                        i.getCreatedAt(), i.getResolvedAt()).toMinutes())
                .average();

        stats.put("total", issueRepository.count());
        stats.put("byStatus", byStatus);
        stats.put("byPriority", byPriority);
        stats.put("averageResolutionHours",
                avgResolutionMinutes.isPresent()
                        ? Math.round(avgResolutionMinutes.getAsDouble()
                                     / 60.0 * 100.0) / 100.0
                        : null);

        return ResponseEntity.ok(stats);
    }


    @PutMapping("/{issueId}/status")
    @Transactional
    public ResponseEntity<MaintenanceIssue> updateStatus(
            @PathVariable String issueId,
            @RequestParam IssueStatus status) {

        MaintenanceIssue issue = findIssue(issueId);
        applyStatus(issue, status);
        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PutMapping("/{issueId}/priority")
    @Transactional
    public ResponseEntity<MaintenanceIssue> updatePriority(
            @PathVariable String issueId,
            @RequestParam IssuePriority priority) {

        MaintenanceIssue issue = findIssue(issueId);

        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot change priority of a closed issue");
        }

        issue.setPriority(priority);
        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PutMapping("/{issueId}")
    @Transactional
    public ResponseEntity<MaintenanceIssue> updateDetails(
            @PathVariable String issueId,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String description) {

        MaintenanceIssue issue = findIssue(issueId);

        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot edit a closed issue");
        }

        if (issueType != null && !issueType.isBlank()) {
            issue.setIssueType(issueType);
        }

        if (description != null) {
            issue.setDescription(description);
        }

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PutMapping("/{issueId}/reassign")
    @Transactional
    public ResponseEntity<MaintenanceIssue> reassign(
            @PathVariable String issueId,
            @RequestParam(required = false) String trainId,
            @RequestParam(required = false) String stationId) {

        MaintenanceIssue issue = findIssue(issueId);

        if (trainId != null) {
            issue.setTrain(orNotFound(
                    trainRepository.findById(trainId), "Train", trainId));
        }

        if (stationId != null) {
            issue.setStation(orNotFound(
                    stationRepository.findById(stationId), "Station", stationId));
        }

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    // -------------------- Workflow shortcuts --------------------

    @PostMapping("/{issueId}/start")
    @Transactional
    public ResponseEntity<MaintenanceIssue> startWork(
            @PathVariable String issueId) {

        MaintenanceIssue issue = findIssue(issueId);

        if (issue.getStatus() != IssueStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only OPEN issues can be started");
        }

        applyStatus(issue, IssueStatus.IN_PROGRESS);
        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PostMapping("/{issueId}/resolve")
    @Transactional
    public ResponseEntity<MaintenanceIssue> resolve(
            @PathVariable String issueId) {

        MaintenanceIssue issue = findIssue(issueId);
        applyStatus(issue, IssueStatus.RESOLVED);

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PostMapping("/{issueId}/close")
    @Transactional
    public ResponseEntity<MaintenanceIssue> close(
            @PathVariable String issueId) {

        MaintenanceIssue issue = findIssue(issueId);
        applyStatus(issue, IssueStatus.CLOSED);

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @PostMapping("/{issueId}/reopen")
    @Transactional
    public ResponseEntity<MaintenanceIssue> reopen(
            @PathVariable String issueId) {

        MaintenanceIssue issue = findIssue(issueId);

        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only RESOLVED issues can be reopened");
        }

        applyStatus(issue, IssueStatus.OPEN);
        return ResponseEntity.ok(issueRepository.save(issue));
    }


    @DeleteMapping("/{issueId}")
    @Transactional
    public ResponseEntity<Void> deleteIssue(
            @PathVariable String issueId) {

        MaintenanceIssue issue = findIssue(issueId);
        issueRepository.delete(issue);

        return ResponseEntity.noContent().build();
    }
}