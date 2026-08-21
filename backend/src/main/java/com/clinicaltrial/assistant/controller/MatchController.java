package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.dto.MatchResultDTO;
import com.clinicaltrial.assistant.dto.ReviewQueueDTO;
import com.clinicaltrial.assistant.service.EligibilityEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MatchController {
    private final EligibilityEngine eligibilityEngine;
    
    @GetMapping("/matches/{id}")
    public ResponseEntity<MatchResultDTO> getMatch(@PathVariable Long id) {
        return ResponseEntity.ok(eligibilityEngine.getMatchResult(id));
    }
    
    @GetMapping("/patients/{patientId}/matches")
    public ResponseEntity<List<MatchResultDTO>> getPatientMatches(@PathVariable Long patientId) {
        return ResponseEntity.ok(eligibilityEngine.getMatchesByPatient(patientId));
    }
    
    @GetMapping("/review-queue")
    public ResponseEntity<List<ReviewQueueDTO>> getReviewQueue() {
        return ResponseEntity.ok(eligibilityEngine.getReviewQueue());
    }
    
    @PostMapping("/matches/{id}/resolve")
    public ResponseEntity<MatchResultDTO> resolveReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(eligibilityEngine.resolveReview(id, body.get("notes"), body.get("decision")));
    }
}
