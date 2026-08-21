package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trial_matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialMatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trial_id")
    private Trial trial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    @Enumerated(EnumType.STRING)
    private MatchStatus status;
    private String reviewNotes;
    @CreationTimestamp
    private LocalDateTime matchedAt;
    @OneToMany(mappedBy = "trialMatch", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CriterionResult> criterionResults = new ArrayList<>();
    
    public enum MatchStatus { ELIGIBLE, NOT_ELIGIBLE, POTENTIALLY_ELIGIBLE, NEEDS_REVIEW, MANUALLY_OVERRIDDEN }
}
