package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.dto.DashboardDTO;
import com.clinicaltrial.assistant.dto.TimelineEventDTO;
import com.clinicaltrial.assistant.model.AuditLog;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import com.clinicaltrial.assistant.repository.PatientRepository;
import com.clinicaltrial.assistant.repository.TrialMatchRepository;
import com.clinicaltrial.assistant.repository.TrialRepository;
import com.clinicaltrial.assistant.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {
    private final PatientRepository patientRepo;
    private final ClinicalEventRepository eventRepo;
    private final TrialRepository trialRepo;
    private final TrialMatchRepository matchRepo;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        LocalDateTime todayMidnight = LocalDate.now().atStartOfDay();
        
        List<TimelineEventDTO> recentEvents = eventRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .stream()
                .map(e -> TimelineEventDTO.builder()
                        .id(e.getId())
                        .time(e.getEventTimestamp())
                        .source(e.getSourceSystem())
                        .type(e.getEventType().name())
                        .name(e.getDisplayName())
                        .value(e.getValue())
                        .numericValue(e.getNumericValue() != null ? e.getNumericValue().toPlainString() : null)
                        .unit(e.getUnit())
                        .sourceRecordId(e.getSourceRecordId())
                        .rawFhirJson(e.getRawFhirJson())
                        .build())
                .collect(Collectors.toList());

        DashboardDTO dto = DashboardDTO.builder()
                .totalPatients(patientRepo.count())
                .totalClinicalEvents(eventRepo.count())
                .totalTrials(trialRepo.count())
                .potentialMatches(matchRepo.countPotentialMatches())
                .needsReview(matchRepo.countNeedsReview())
                .newPatientsToday(patientRepo.countByCreatedAtAfter(todayMidnight))
                .newEventsToday(eventRepo.countByCreatedAtAfter(todayMidnight))
                .recentEvents(recentEvents)
                .build();
                
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getAll(page, size));
    }
    @GetMapping("/audit-logs/verify")
    public ResponseEntity<Map<String,Object>> verifyAuditLogs() { return ResponseEntity.ok(auditService.verifyIntegrity()); }
}
