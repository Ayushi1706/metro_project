package org.spring.metro.models.dto;

import org.spring.metro.models.enums.IssueStatus;

public record MaintenanceIssueStatusRequestDto(
        IssueStatus status
) {}