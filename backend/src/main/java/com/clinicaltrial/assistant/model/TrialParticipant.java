package com.clinicaltrial.assistant.model;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.CreationTimestamp; import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name="trial_participants", uniqueConstraints=@UniqueConstraint(columnNames={"trial_id","patient_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TrialParticipant {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="trial_id") private Trial trial;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="patient_id") private Patient patient;
 private String status; private LocalDate enrolledDate; private LocalDate completedDate; private String treatmentArm; private String consentVersion;
 @Builder.Default private boolean baselineComplete=false; private BigDecimal adherencePercent; private LocalDate nextVisitDate; private String dsmbStatus; private String withdrawalReason;
 @CreationTimestamp private LocalDateTime createdAt;
}
