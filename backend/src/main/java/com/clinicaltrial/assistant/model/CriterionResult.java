package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "criterion_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterionResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trial_match_id")
    private TrialMatch trialMatch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id")
    private TrialCriterion criterion;
    @Enumerated(EnumType.STRING)
    private ResultStatus status;
    private String patientValue;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_event_id")
    private ClinicalEvent clinicalEvent;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    private BigDecimal gapToPass;
    private String gapDescription;
    
    public enum ResultStatus { PASS, FAIL, MISSING, REVIEW_REQUIRED }
}
