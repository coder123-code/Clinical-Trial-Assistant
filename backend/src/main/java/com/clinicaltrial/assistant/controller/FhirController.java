package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.dto.FhirIngestRequest;
import com.clinicaltrial.assistant.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
@Slf4j
public class FhirController {
    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper;
    
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingest(@RequestBody Map<String, Object> rawBody) {
        try {
            String rawJson = objectMapper.writeValueAsString(rawBody);
            FhirIngestRequest req = objectMapper.convertValue(rawBody, FhirIngestRequest.class);
            String result = ingestionService.ingest(req, rawJson);
            return ResponseEntity.ok(Map.of("status", "success", "message", result));
        } catch (Exception e) {
            log.error("FHIR ingestion failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
