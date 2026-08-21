package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final ClinicalEventRepository repo;
    
    public boolean isDuplicate(String sourceSystem, String sourceRecordId) {
        return repo.findBySourceSystemAndSourceRecordId(sourceSystem, sourceRecordId).isPresent();
    }
}
