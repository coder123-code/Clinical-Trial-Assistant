package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.dto.PatientDTO;
import com.clinicaltrial.assistant.dto.TimelineEventDTO;
import com.clinicaltrial.assistant.service.PatientService;
import com.clinicaltrial.assistant.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final TimelineService timelineService;
    
    @GetMapping
    public ResponseEntity<Page<PatientDTO>> getPatients(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(patientService.getAllPatients(search, page, size));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable String id) {
        // Support both canonical ID (P000001) and numeric ID
        if (id.startsWith("P")) {
            return ResponseEntity.ok(patientService.getPatientByCanonicalId(id));
        } else {
            return ResponseEntity.ok(patientService.getPatientById(Long.parseLong(id)));
        }
    }
    
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEventDTO>> getTimeline(@PathVariable String id) {
        if (id.startsWith("P")) {
            return ResponseEntity.ok(timelineService.getTimeline(id));
        } else {
            return ResponseEntity.ok(timelineService.getTimelineByPatientId(Long.parseLong(id)));
        }
    }
}
