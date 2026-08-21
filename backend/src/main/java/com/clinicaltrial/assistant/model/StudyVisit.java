package com.clinicaltrial.assistant.model;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="study_visits",indexes=@Index(name="idx_visit_participant_date",columnList="participant_id,scheduledDate"))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudyVisit {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="participant_id") private TrialParticipant participant;
 private String visitName; private LocalDate scheduledDate; private LocalDate completedDate; private String status;
 private String doseAdministered; private BigDecimal adherencePercent; private String labName; private BigDecimal labValue; private String labUnit; private BigDecimal efficacyScore;
 @Column(columnDefinition="TEXT") private String notes; @Column(columnDefinition="TEXT") private String protocolDeviation;
}
