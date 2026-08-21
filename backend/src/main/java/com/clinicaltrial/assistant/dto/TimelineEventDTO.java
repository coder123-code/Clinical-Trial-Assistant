package com.clinicaltrial.assistant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TimelineEventDTO {
    private Long id;
    private String patientName;
    private String canonicalPatientId;
    private LocalDateTime time;
    private String source;
    private String type;
    private String name;
    private String value;
    private String numericValue;
    private String unit;
    private String sourceRecordId;
    private String rawFhirJson;
    private String sourceFormat;
    private String rawSourceText;
    private String sourceDocumentName;
}
