package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String trialCode;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String originalEligibilityText;
    @OneToMany(mappedBy = "trial", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrialCriterion> criteria = new ArrayList<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
}
