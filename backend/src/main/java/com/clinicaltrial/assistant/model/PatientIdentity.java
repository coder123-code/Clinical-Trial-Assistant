package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_identities", indexes = {
    @Index(name = "idx_identity_source", columnList = "sourceSystem,sourcePatientId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_identity", columnNames = {"sourceSystem", "sourcePatientId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdentity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    private String sourceSystem;
    private String sourcePatientId;
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;
    private Integer matchScore;
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum MatchStatus { CONFIRMED, REVIEW_REQUIRED, NEW_PATIENT }
}
