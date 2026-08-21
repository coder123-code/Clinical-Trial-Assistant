package com.clinicaltrial.assistant.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="adverse_events",indexes=@Index(name="idx_ae_participant",columnList="participant_id"))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdverseEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="participant_id") private TrialParticipant participant;
 private LocalDate onsetDate; private String severity; private String relatedness; private String status; private boolean serious;
 @Column(columnDefinition="TEXT") private String description; private String actionTaken;
}
