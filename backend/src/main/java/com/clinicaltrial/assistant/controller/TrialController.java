package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.dto.MatchResultDTO;
import com.clinicaltrial.assistant.dto.TrialDTO;
import com.clinicaltrial.assistant.service.EligibilityEngine;
import com.clinicaltrial.assistant.service.TrialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;

@RestController
@RequestMapping("/api/trials")
@RequiredArgsConstructor
public class TrialController {
    private final TrialService trialService;
    private final EligibilityEngine eligibilityEngine;

    @PostMapping("/upload-pdf")
    public ResponseEntity<Map<String, String>> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Empty file uploaded"));
            }
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                return ResponseEntity.ok(Map.of("text", text));
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to parse PDF: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TrialDTO>> getAll() {
        return ResponseEntity.ok(trialService.getAll());
    }
    
    @PostMapping
    public ResponseEntity<TrialDTO> create(@Valid @RequestBody TrialDTO dto) {
        return ResponseEntity.ok(trialService.createTrial(dto));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TrialDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trialService.getById(id));
    }
    
    @PostMapping("/{trialId}/extract-criteria")
    public ResponseEntity<TrialDTO> extractCriteria(
            @PathVariable Long trialId,
            @RequestBody Map<String, String> body) {
        String text = body.get("text");
        return ResponseEntity.ok(trialService.extractAndSaveCriteria(trialId, text));
    }
    @PostMapping("/{trialId}/confirm-criteria")
    public ResponseEntity<TrialDTO> confirmCriteria(@PathVariable Long trialId) { return ResponseEntity.ok(trialService.confirmCriteria(trialId)); }

    @PostMapping("/{trialId}/confirm-and-screen")
    public ResponseEntity<List<MatchResultDTO>> confirmAndScreen(@PathVariable Long trialId) {
        trialService.confirmCriteria(trialId);
        return ResponseEntity.ok(eligibilityEngine.runCohortMatching(trialId));
    }
    
    @PostMapping("/{trialId}/match/{patientId}")
    public ResponseEntity<MatchResultDTO> runMatching(
            @PathVariable Long trialId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(eligibilityEngine.runMatching(trialId, patientId));
    }
    
    @GetMapping("/{trialId}/matches")
    public ResponseEntity<List<MatchResultDTO>> getTrialMatches(@PathVariable Long trialId) {
        return ResponseEntity.ok(eligibilityEngine.getMatchesByTrial(trialId));
    }
    @GetMapping("/{trialId}/gap-analysis")
    public ResponseEntity<Map<String,Object>> getGapAnalysis(@PathVariable Long trialId) { return ResponseEntity.ok(eligibilityEngine.getGapAnalysis(trialId)); }
}
