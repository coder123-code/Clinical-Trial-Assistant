package com.clinicaltrial.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {
    private final RestTemplate restTemplate;
    
    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    public Map<String, Object> extractCriteria(String eligibilityText) {
        try {
            Map<String, String> req = new HashMap<>();
            req.put("text", eligibilityText);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                aiServiceUrl + "/extract-criteria", req, Map.class);
            log.info("AI service returned criteria extraction");
            return response;
        } catch (Exception e) {
            log.error("AI service call failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage());
        }
    }
}
