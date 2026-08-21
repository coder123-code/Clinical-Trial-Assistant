package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.TimelineEventDTO;
import com.clinicaltrial.assistant.model.ClinicalEvent;
import com.clinicaltrial.assistant.model.Patient;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import com.clinicaltrial.assistant.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final ClinicalEventRepository eventRepo;
    private final PatientRepository patientRepo;
    
    public List<TimelineEventDTO> getTimeline(String canonicalPatientId) {
        Patient patient = patientRepo.findByCanonicalPatientId(canonicalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found: " + canonicalPatientId));
        List<ClinicalEvent> events = eventRepo.findByPatientIdOrderByEventTimestampAsc(patient.getId());
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    public List<TimelineEventDTO> getTimelineByPatientId(Long patientId) {
        List<ClinicalEvent> events = eventRepo.findByPatientIdOrderByEventTimestampAsc(patientId);
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    private TimelineEventDTO toDTO(ClinicalEvent e) {
        return TimelineEventDTO.builder()
            .id(e.getId())
            .patientName(e.getPatient().getFirstName() + " " + e.getPatient().getLastName())
            .canonicalPatientId(e.getPatient().getCanonicalPatientId())
            .time(e.getEventTimestamp())
            .source(e.getSourceSystem())
            .type(e.getEventType().name())
            .name(e.getDisplayName())
            .value(e.getValue())
            .numericValue(e.getNumericValue() != null ? e.getNumericValue().toPlainString() : null)
            .unit(e.getUnit())
            .sourceRecordId(e.getSourceRecordId())
            .rawFhirJson(e.getRawFhirJson())
            .sourceFormat(e.getSourceFormat())
            .rawSourceText(e.getRawSourceText())
            .sourceDocumentName(e.getSourceDocumentName())
            .build();
    }
}
