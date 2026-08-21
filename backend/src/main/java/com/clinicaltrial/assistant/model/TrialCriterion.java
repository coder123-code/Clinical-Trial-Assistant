package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "trial_criteria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialCriterion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trial_id")
    private Trial trial;
    @Enumerated(EnumType.STRING)
    private CriterionType criterionType;
    private String clinicalField;
    private String operator; // BETWEEN, GREATER_THAN, LESS_THAN, EQUALS
    private String value;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String unit;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Builder.Default
    private boolean reviewedByHuman = false;
    
    public enum CriterionType { INCLUSION, EXCLUSION }
}
