package com.clinicaltrial.assistant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewQueueDTO {
    private Long matchId;
    private String patientName;
    private String canonicalPatientId;
    private String trialTitle;
    private String trialCode;
    private String currentStatus;
    private String reviewNotes;
    private LocalDateTime matchedAt;
    private long missingCriteria;
    private long failedCriteria;
    private long totalCriteria;
}
