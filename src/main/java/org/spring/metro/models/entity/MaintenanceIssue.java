package org.spring.metro.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.spring.metro.models.enums.IssuePriority;
import org.spring.metro.models.enums.IssueStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_issue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceIssue {

    @Id
    @Column(name = "issue_id")
    private String issueId;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "reported_id", nullable = false)
    private User reportedBy;

    @Column(name = "issue_type", nullable = false)
    private String issueType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}