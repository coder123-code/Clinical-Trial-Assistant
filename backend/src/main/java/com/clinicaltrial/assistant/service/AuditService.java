package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.model.AuditLog;
import com.clinicaltrial.assistant.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditLogRepository repo;
    
    public synchronized void log(String action, String entityType, String entityId, String details) {
        try {
            String previous = repo.findTopByOrderByIdDesc().map(AuditLog::getCurrentHash).orElse("GENESIS");
            // Match MySQL DATETIME(6) precision so a persisted timestamp hashes identically.
            LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
            String current = hash(previous + action + entityId + timestamp);
            repo.save(AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .performedBy("SYSTEM")
                .timestamp(timestamp)
                .previousHash(previous)
                .currentHash(current)
                .build());
        } catch (Exception e) {
            log.warn("Failed to write audit log: {}", e.getMessage());
        }
    }
    private String hash(String value) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }

    public Map<String,Object> verifyIntegrity() {
        var logs = repo.findAllByOrderByIdAsc(); String expected = "GENESIS";
        for (AuditLog item : logs) {
            try {
                String calculated = hash(expected + item.getAction() + item.getEntityId() + item.getTimestamp());
                if (!expected.equals(item.getPreviousHash()) || !calculated.equals(item.getCurrentHash())) return Map.of("valid", false, "checkedEntries", logs.size(), "brokenAtId", item.getId());
                expected = item.getCurrentHash();
            } catch (Exception e) { return Map.of("valid", false, "error", e.getMessage()); }
        }
        Map<String,Object> result = new LinkedHashMap<>(); result.put("valid", true); result.put("checkedEntries", logs.size()); result.put("chainHead", expected); return result;
    }
    
    public Page<AuditLog> getAll(int page, int size) {
        return repo.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }
}
