package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.MaintenanceIssueDto;
import org.spring.metro.models.dto.MaintenanceIssueRequestDto;
import org.spring.metro.models.dto.MaintenanceIssueStatusRequestDto;
import org.spring.metro.service.MaintenanceIssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-issues")
@RequiredArgsConstructor
public class MaintenanceIssueController {

    private final MaintenanceIssueService maintenanceIssueService;

    @PostMapping
    public ResponseEntity<MaintenanceIssueDto> createIssue(
            @RequestBody MaintenanceIssueRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        maintenanceIssueService.createIssue(request)
                );
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<MaintenanceIssueDto> getIssueById(
            @PathVariable String issueId) {

        return ResponseEntity.ok(
                maintenanceIssueService.getIssueById(issueId)
        );
    }

    @PutMapping("/{issueId}/status")
    public ResponseEntity<MaintenanceIssueDto> updateStatus(
            @PathVariable String issueId,
            @RequestBody MaintenanceIssueStatusRequestDto request) {

        return ResponseEntity.ok(
                maintenanceIssueService.updateStatus(
                        issueId,
                        request.status()
                )
        );
    }
}