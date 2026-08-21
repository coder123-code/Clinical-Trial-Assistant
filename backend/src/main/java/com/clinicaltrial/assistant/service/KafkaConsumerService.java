package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.FhirIngestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaConsumerService {
    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "clinical-events", groupId = "clinical-trial-app")
    public void consume(String message, Acknowledgment ack) {
        try {
            log.info("Received Kafka message: {}", message.substring(0, Math.min(100, message.length())));
            FhirIngestRequest req = objectMapper.readValue(message, FhirIngestRequest.class);
            ingestionService.ingest(req, message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
            ack.acknowledge(); // acknowledge to avoid poison pill — add DLT later
        }
    }
}
