package com.clinicaltrial.assistant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MatchResultDTO {
    private Long id;
    private String status;
    private LocalDateTime matchedAt;
    private String reviewNotes;
    // trial info
    private Long trialId;
    private String trialTitle;
    private String trialCode;
    // patient info
    private Long patientId;
    private String patientName;
    private String canonicalPatientId;
    // criterion results
    private List<CriterionResultDTO> criterionResults;
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CriterionResultDTO {
        private Long id;
        private String criterionType;
        private String clinicalField;
        private String operator;
        private String criterionValue;
        private String criterionMin;
        private String criterionMax;
        private String unit;
        private String criterionDescription;
        private String status;
        private String patientValue;
        private String sourceSystem;
        private Long sourceEventId;
        private String sourceRecordId;
        private LocalDateTime eventTimestamp;
        private String rawFhirJson;
        private String sourceFormat;
        private String rawSourceText;
        private String sourceDocumentName;
        private String explanation;
        private String gapToPass;
        private String gapDescription;
    }
}
