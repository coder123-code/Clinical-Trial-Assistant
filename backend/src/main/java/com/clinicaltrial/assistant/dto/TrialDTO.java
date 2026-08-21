package com.clinicaltrial.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TrialDTO {
    private Long id;
    @NotBlank private String title;
    private String trialCode;
    private String description;
    private String originalEligibilityText;
    private LocalDateTime createdAt;
    private List<CriterionDTO> criteria;
    private long matchCount;
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CriterionDTO {
        private Long id;
        private String criterionType;
        private String clinicalField;
        private String operator;
        private String value;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private String unit;
        private String description;
        private boolean reviewedByHuman;
    }
}
