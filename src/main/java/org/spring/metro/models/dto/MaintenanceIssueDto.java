package org.spring.metro.models.dto;

import java.time.LocalDateTime;

public record MaintenanceIssueDto(
        String issueId,
        String trainId,
        String stationId,
        String reportedBy,
        String issueType,
        String description,
        String status,
        String priority,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}