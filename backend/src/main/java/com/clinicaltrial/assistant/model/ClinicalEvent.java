package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinical_events", indexes = {
    @Index(name = "idx_event_patient", columnList = "patient_id"),
    @Index(name = "idx_event_timestamp", columnList = "eventTimestamp"),
    @Index(name = "idx_event_code", columnList = "code"),
    @Index(name = "idx_event_source", columnList = "sourceSystem,sourceRecordId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_event_source_record", columnNames = {"sourceSystem", "sourceRecordId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String code;
    private String displayName;
    private String value;
    private BigDecimal numericValue;
    private String unit;
    private LocalDateTime eventTimestamp;
    private String sourceSystem;
    private String sourceRecordId;
    @Column(columnDefinition = "TEXT")
    private String rawFhirJson;
    @Builder.Default
    private String sourceFormat = "FHIR";
    @Column(columnDefinition = "TEXT")
    private String rawSourceText;
    private String sourceDocumentName;
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum EventType { OBSERVATION, CONDITION, ENCOUNTER, MEDICATION, PROCEDURE }
}
