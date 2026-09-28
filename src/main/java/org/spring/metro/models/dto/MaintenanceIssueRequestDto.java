package org.spring.metro.models.dto;

import org.spring.metro.models.enums.IssuePriority;

public record MaintenanceIssueRequestDto(
        String trainId,
        String stationId,
        Long reportedBy,
        String issueType,
        String description,
        IssuePriority priority
) {}