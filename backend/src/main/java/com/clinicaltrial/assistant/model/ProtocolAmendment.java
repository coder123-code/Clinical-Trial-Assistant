package com.clinicaltrial.assistant.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="protocol_amendments") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProtocolAmendment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="trial_id") private Trial trial;
 private String version; private String title; @Column(columnDefinition="TEXT") private String summary; private LocalDate effectiveDate;
 private String approvalStatus; private boolean requiresReconsent; private String approvedBy;
}
