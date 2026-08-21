package com.clinicaltrial.assistant.service;
import com.clinicaltrial.assistant.model.*; import com.clinicaltrial.assistant.repository.*; import lombok.RequiredArgsConstructor; import org.springframework.boot.*; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
@Component @Order(2) @RequiredArgsConstructor
public class TrialOperationsSeeder implements ApplicationRunner {
 private final TrialRepository trials; private final PatientRepository patients; private final TrialParticipantRepository participants; private final StudyVisitRepository visits; private final AdverseEventRepository adverse; private final ProtocolAmendmentRepository amendments;
 @Override @Transactional public void run(ApplicationArguments args){if(participants.count()>0)return;Trial t=trials.findAll().stream().findFirst().orElse(null);List<Patient>ps=patients.findAll();if(t==null||ps.size()<4)return;
  TrialParticipant john=add(t,ps.get(0),"ACTIVE","Intervention 20 mg",new BigDecimal("96.5"),LocalDate.now().plusDays(18),"CONTINUE — no safety signal");
  TrialParticipant jane=add(t,ps.get(1),"MONITORING","Intervention 10 mg",new BigDecimal("88.0"),LocalDate.now().plusDays(7),"ENHANCED MONITORING");
  TrialParticipant maya=add(t,ps.get(2),"SCREENING","Pending randomization",null,LocalDate.now().plusDays(3),"PENDING BASELINE REVIEW");
  TrialParticipant robert=add(t,ps.get(3),"WITHDRAWN","Intervention 20 mg",new BigDecimal("72.0"),null,"CLOSED");robert.setWithdrawalReason("Participant withdrew consent after month 3 visit");participants.save(robert);
  visit(john,"Baseline / Day 0",LocalDate.now().minusDays(170),"COMPLETED","20 mg",new BigDecimal("100"),"HbA1c",new BigDecimal("8.2"),new BigDecimal("42"),null);
  visit(john,"Month 1 safety",LocalDate.now().minusDays(140),"COMPLETED","20 mg",new BigDecimal("98"),"HbA1c",new BigDecimal("7.9"),new BigDecimal("48"),null);
  visit(john,"Month 3 efficacy",LocalDate.now().minusDays(80),"COMPLETED","20 mg",new BigDecimal("96"),"HbA1c",new BigDecimal("7.4"),new BigDecimal("61"),null);
  visit(john,"Month 6 assessment",LocalDate.now().plusDays(18),"SCHEDULED",null,null,"HbA1c",null,null,null);
  visit(jane,"Baseline / Day 0",LocalDate.now().minusDays(120),"COMPLETED","10 mg",new BigDecimal("100"),"HbA1c",new BigDecimal("9.1"),new BigDecimal("38"),null);
  visit(jane,"Month 1 safety",LocalDate.now().minusDays(90),"COMPLETED","10 mg",new BigDecimal("88"),"eGFR",new BigDecimal("24"),new BigDecimal("41"),"Safety laboratory collected 2 days outside protocol window");
  visit(jane,"Unscheduled renal review",LocalDate.now().plusDays(7),"SCHEDULED",null,null,"eGFR",null,null,null);
  visit(maya,"Screening visit",LocalDate.now().plusDays(3),"SCHEDULED",null,null,"eGFR",null,null,null);
  ae(john,LocalDate.now().minusDays(112),"MILD","POSSIBLE",false,"Transient nausea after dosing","Supportive care; treatment continued","RESOLVED");
  ae(jane,LocalDate.now().minusDays(18),"MODERATE","PROBABLE",false,"Renal laboratory trend requires closer observation","Dose held pending investigator and safety review","OPEN");
  amendments.save(ProtocolAmendment.builder().trial(t).version("2.0").title("Enhanced renal monitoring").summary("Adds an unscheduled renal safety assessment and revised dose-hold thresholds following aggregate safety review.").effectiveDate(LocalDate.now().minusDays(20)).approvalStatus("APPROVED").requiresReconsent(true).approvedBy("IRB & Sponsor Medical Monitor").build());
  amendments.save(ProtocolAmendment.builder().trial(t).version("1.1").title("Laboratory window clarification").summary("Clarifies acceptable collection windows for month 1 and month 3 laboratory panels.").effectiveDate(LocalDate.now().minusDays(100)).approvalStatus("SUPERSEDED").requiresReconsent(false).approvedBy("Sponsor Medical Monitor").build());
 }
 private TrialParticipant add(Trial t,Patient p,String status,String arm,BigDecimal adherence,LocalDate next,String dsmb){return participants.save(TrialParticipant.builder().trial(t).patient(p).status(status).enrolledDate(LocalDate.now().minusDays(180)).treatmentArm(arm).consentVersion("2.0").baselineComplete(!"SCREENING".equals(status)).adherencePercent(adherence).nextVisitDate(next).dsmbStatus(dsmb).build());}
 private void visit(TrialParticipant p,String name,LocalDate date,String status,String dose,BigDecimal adherence,String lab,BigDecimal value,BigDecimal efficacy,String deviation){visits.save(StudyVisit.builder().participant(p).visitName(name).scheduledDate(date).completedDate("COMPLETED".equals(status)?date:null).status(status).doseAdministered(dose).adherencePercent(adherence).labName(lab).labValue(value).labUnit("HbA1c".equals(lab)?"%":"mL/min").efficacyScore(efficacy).notes("Source-verified study visit record").protocolDeviation(deviation).build());}
 private void ae(TrialParticipant p,LocalDate date,String severity,String related,boolean serious,String desc,String action,String status){adverse.save(AdverseEvent.builder().participant(p).onsetDate(date).severity(severity).relatedness(related).serious(serious).description(desc).actionTaken(action).status(status).build());}
}
