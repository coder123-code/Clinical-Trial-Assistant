package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_canonical_id", columnList = "canonicalPatientId"),
    @Index(name = "idx_patient_dob", columnList = "dateOfBirth")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String canonicalPatientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConsentStatus consentStatus = ConsentStatus.PENDING;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public enum ConsentStatus { NOT_ASKED, PENDING, CONSENTED, DECLINED }
}
