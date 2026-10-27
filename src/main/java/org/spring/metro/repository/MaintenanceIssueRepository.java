package org.spring.metro.repository;

import org.spring.metro.models.entity.MaintenanceIssue;
import org.spring.metro.models.entity.Train;
import org.spring.metro.models.entity.Station;
import org.spring.metro.models.enums.IssuePriority;
import org.spring.metro.models.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceIssueRepository
        extends JpaRepository<MaintenanceIssue, String> {

    List<MaintenanceIssue> findByTrain(Train train);

    List<MaintenanceIssue> findByStation(Station station);

    List<MaintenanceIssue> findByStatus(IssueStatus status);

    List<MaintenanceIssue> findByPriority(IssuePriority priority);

    List<MaintenanceIssue> findByStatusAndPriority(
            IssueStatus status, IssuePriority priority);

    List<MaintenanceIssue> findByStatusNotAndCreatedAtBefore(
            IssueStatus status, LocalDateTime cutoff);

    List<MaintenanceIssue> findByCreatedAtBetween(
            LocalDateTime from, LocalDateTime to);

    long countByStatus(IssueStatus status);

    long countByPriority(IssuePriority priority);
}