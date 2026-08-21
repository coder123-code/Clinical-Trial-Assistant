package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.FhirIngestRequest;
import com.clinicaltrial.assistant.model.Trial;
import com.clinicaltrial.assistant.model.TrialCriterion;
import com.clinicaltrial.assistant.repository.PatientRepository;
import com.clinicaltrial.assistant.repository.TrialRepository;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DemoDataSeeder implements ApplicationRunner {
    
    private final PatientRepository patientRepo;
    private final TrialRepository trialRepo;
    private final IngestionService ingestionService;
    private final EligibilityEngine eligibilityEngine;
    private final ClinicalEventRepository eventRepo;
    
    @Override
    public void run(ApplicationArguments args) {
        seedDemoData();
    }
    
    @Transactional
    public void seedDemoData() {
        if (patientRepo.count() > 0) {
            log.info("Database already seeded, skipping");
            return;
        }
        
        log.info("Seeding demo data...");
        try {
            seedPatientData();
            seedClinicalEvents();
            seedTrialData();
            seedMatchResults();
            log.info("Demo data seeding completed");
        } catch (Exception e) {
            log.error("Failed to seed demo data: {}", e.getMessage(), e);
        }
    }
    
    private void seedPatientData() {
        // John Smith
        ingestPatient("HOSPITAL_A", "A123", "John", "Smith", "1970-12-03", "male");
        ingestPatient("HOSPITAL_B", "B928", "John", "Smith", "1970-12-03", "male");
        ingestPatient("LAB_A", "L991", "John", "Smith", "1970-12-03", "male");
        ingestPatient("LAB_B", "LB472", "John", "Smith", "1970-12-03", "male");
        
        // Jane Doe
        ingestPatient("HOSPITAL_A", "A456", "Jane", "Doe", "1985-06-15", "female");

        ingestPatient("HOSPITAL_C", "C204", "Maya", "Patel", "1992-02-11", "female");
        ingestPatient("COMMUNITY_CLINIC", "CC-882", "Maya", "Patel", "1992-02-11", "female");
        ingestPatient("HOSPITAL_B", "B310", "Robert", "Chen", "1958-09-22", "male");
        ingestPatient("HOSPITAL_A", "A719", "Elena", "Garcia", "1977-04-08", "female");
        ingestPatient("HOSPITAL_C", "C551", "Aisha", "Khan", "1966-11-29", "female");
        ingestPatient("HOSPITAL_A", "A884", "David", "Williams", "1949-01-17", "male");
        ingestPatient("HOSPITAL_B", "B662", "Priya", "Raman", "1989-08-03", "female");
        ingestPatient("HOSPITAL_C", "C903", "Marcus", "Johnson", "1973-05-26", "male");
        ingestPatient("COMMUNITY_CLINIC", "CC-441", "Sofia", "Martinez", "1997-12-14", "female");
        ingestPatient("HOSPITAL_A", "A991", "Noah", "Brown", "1961-07-05", "male");
    }
    
    private void ingestPatient(String sourceSystem, String sourceId, String firstName, String lastName, String dob, String gender) {
        FhirIngestRequest req = new FhirIngestRequest();
        req.setResourceType("Patient");
        req.setId(sourceId);
        req.setSourceSystem(sourceSystem);
        req.setBirthDate(dob);
        req.setGender(gender);
        req.setName(List.of(Map.of("family", lastName, "given", List.of(firstName))));
        req.setIdentifier(List.of(Map.of("system", sourceSystem, "value", sourceId)));
        ingestionService.ingest(req, String.format(
                "{\"resourceType\":\"Patient\",\"id\":\"%s\",\"sourceSystem\":\"%s\",\"name\":[{\"family\":\"%s\",\"given\":[\"%s\"]}],\"birthDate\":\"%s\",\"gender\":\"%s\"}",
                sourceId, sourceSystem, lastName, firstName, dob, gender));
    }
    
    private void seedClinicalEvents() {
        // John Smith Events
        ingestObservation("HOSPITAL_A", "OBS-A001", "A123", "HbA1c", 8.2, "%", "2026-08-20T08:00:00");
        ingestObservation("LAB_A", "OBS-L001", "L991", "Glucose", 180.0, "mg/dL", "2026-08-20T10:00:00");
        ingestObservation("HOSPITAL_B", "OBS-B001", "B928", "Creatinine", 1.1, "mg/dL", "2026-08-20T13:00:00");
        ingestObservationString("LAB_B", "OBS-LB001", "LB472", "CBC", "Normal", "2026-08-20T16:00:00");
        ingestCondition("HOSPITAL_A", "COND-A001", "A123", "Type 2 Diabetes Mellitus", "2026-08-19T10:00:00");
        ingestObservation("LAB_A", "OBS-L002", "L991", "eGFR", 45.0, "mL/min", "2026-08-20T10:30:00");
        ingestMedication("HOSPITAL_B", "MED-B001", "B928", "Metformin 500mg", "2026-08-18T09:00:00");
        attachDocumentSource("HOSPITAL_B", "MED-B001", "NOTE", "endocrinology-note-2026-08-18.txt", "ENDOCRINOLOGY PROGRESS NOTE\nPatient: John Smith\nDate: 18 Aug 2026\n\nKnown type 2 diabetes mellitus. HbA1c was checked this week. Patient continues metformin 500 mg and reports good adherence. Renal panel requested before study screening.\n\nAssessment: T2DM, stable on oral therapy.\nSigned: A. Rivera, MD");
        attachDocumentSource("LAB_B", "OBS-LB001", "LAB_PDF", "lab-report-LB472-2026-08-20.pdf", "LAB B — VERIFIED LABORATORY REPORT\nPatient reference: LB472\nCollection: 20 Aug 2026 16:00\nAccession: OBS-LB001\n\nCOMPLETE BLOOD COUNT\nOverall interpretation: Normal\n\nThis text was extracted from the original digital PDF and preserved with the clinical event.");
        
        // Jane Doe Events
        ingestObservation("HOSPITAL_A", "OBS-A002", "A456", "HbA1c", 9.1, "%", "2026-08-19T09:00:00");
        ingestCondition("HOSPITAL_A", "COND-A002", "A456", "Type 2 Diabetes Mellitus", "2026-08-15T14:00:00");
        ingestObservation("HOSPITAL_A", "OBS-A003", "A456", "eGFR", 22.0, "mL/min", "2026-08-19T09:30:00");

        // Rich synthetic cohort: eligible, ineligible, and missing-data cases
        ingestCondition("HOSPITAL_C", "COND-C204", "C204", "Type 2 Diabetes Mellitus", "2026-08-12T11:00:00");
        ingestObservation("COMMUNITY_CLINIC", "OBS-CC882-1", "CC-882", "HbA1c", 7.6, "%", "2026-08-18T08:20:00");
        // Maya intentionally has no eGFR, creating a human-review case.

        ingestCondition("HOSPITAL_B", "COND-B310", "B310", "Type 2 Diabetes Mellitus", "2026-07-02T10:00:00");
        ingestObservation("HOSPITAL_B", "OBS-B310-1", "B310", "HbA1c", 8.7, "%", "2026-08-17T09:00:00");
        ingestObservation("HOSPITAL_B", "OBS-B310-2", "B310", "eGFR", 18.0, "mL/min", "2026-08-17T09:05:00");

        ingestCondition("HOSPITAL_A", "COND-A719", "A719", "Type 2 Diabetes Mellitus", "2025-11-14T13:00:00");
        ingestObservation("HOSPITAL_A", "OBS-A719-1", "A719", "HbA1c", 6.4, "%", "2026-08-16T07:45:00");
        ingestObservation("HOSPITAL_A", "OBS-A719-2", "A719", "eGFR", 82.0, "mL/min", "2026-08-16T07:50:00");

        ingestCondition("HOSPITAL_C", "COND-C551", "C551", "Hypertension", "2024-03-21T12:00:00");
        ingestObservation("HOSPITAL_C", "OBS-C551-1", "C551", "Systolic Blood Pressure", 152.0, "mmHg", "2026-08-15T10:30:00");
        ingestObservation("HOSPITAL_C", "OBS-C551-2", "C551", "BMI", 31.2, "kg/m2", "2026-08-15T10:35:00");

        ingestCondition("HOSPITAL_A", "COND-A884", "A884", "Chronic Kidney Disease Stage 3", "2025-05-10T09:00:00");
        ingestObservation("HOSPITAL_A", "OBS-A884-1", "A884", "eGFR", 38.0, "mL/min", "2026-08-14T08:00:00");
        ingestObservation("HOSPITAL_A", "OBS-A884-2", "A884", "Creatinine", 1.7, "mg/dL", "2026-08-14T08:05:00");

        ingestCondition("HOSPITAL_B", "COND-B662", "B662", "Hypertension", "2026-01-03T16:00:00");
        ingestObservation("HOSPITAL_B", "OBS-B662-1", "B662", "Systolic Blood Pressure", 146.0, "mmHg", "2026-08-13T09:10:00");
        ingestMedication("HOSPITAL_B", "MED-B662-1", "B662", "Lisinopril 10mg", "2026-08-13T09:15:00");

        ingestCondition("HOSPITAL_C", "COND-C903", "C903", "Type 2 Diabetes Mellitus", "2023-06-09T14:30:00");
        ingestObservation("HOSPITAL_C", "OBS-C903-1", "C903", "HbA1c", 9.4, "%", "2026-08-12T07:30:00");
        ingestObservation("HOSPITAL_C", "OBS-C903-2", "C903", "eGFR", 64.0, "mL/min", "2026-08-12T07:35:00");

        ingestObservation("COMMUNITY_CLINIC", "OBS-CC441-1", "CC-441", "BMI", 24.8, "kg/m2", "2026-08-11T11:20:00");
        ingestCondition("HOSPITAL_A", "COND-A991", "A991", "Chronic Kidney Disease Stage 3", "2024-10-08T10:00:00");
        ingestObservation("HOSPITAL_A", "OBS-A991-1", "A991", "eGFR", 29.0, "mL/min", "2026-08-10T08:40:00");
    }

    private void attachDocumentSource(String source, String recordId, String format, String fileName, String text) {
        eventRepo.findBySourceSystemAndSourceRecordId(source, recordId).ifPresent(event -> { event.setSourceFormat(format); event.setSourceDocumentName(fileName); event.setRawSourceText(text); eventRepo.save(event); });
    }
    
    private void ingestObservation(String source, String id, String patientId, String name, double value, String unit, String date) {
        FhirIngestRequest req = new FhirIngestRequest();
        req.setResourceType("Observation");
        req.setId(id);
        req.setSourceSystem(source);
        req.setSubject(Map.of("reference", "Patient/" + patientId));
        req.setCode(Map.of("text", name));
        req.setValueQuantity(Map.of("value", value, "unit", unit));
        req.setEffectiveDateTime(date);
        ingestionService.ingest(req, String.format(
                "{\"resourceType\":\"Observation\",\"id\":\"%s\",\"sourceSystem\":\"%s\",\"subject\":{\"reference\":\"Patient/%s\"},\"code\":{\"text\":\"%s\"},\"valueQuantity\":{\"value\":%s,\"unit\":\"%s\"},\"effectiveDateTime\":\"%s\"}",
                id, source, patientId, name, value, unit, date));
    }
    
    private void ingestObservationString(String source, String id, String patientId, String name, String value, String date) {
        FhirIngestRequest req = new FhirIngestRequest();
        req.setResourceType("Observation");
        req.setId(id);
        req.setSourceSystem(source);
        req.setSubject(Map.of("reference", "Patient/" + patientId));
        req.setCode(Map.of("text", name));
        req.setValueString(value);
        req.setEffectiveDateTime(date);
        ingestionService.ingest(req, String.format(
                "{\"resourceType\":\"Observation\",\"id\":\"%s\",\"sourceSystem\":\"%s\",\"subject\":{\"reference\":\"Patient/%s\"},\"code\":{\"text\":\"%s\"},\"valueString\":\"%s\",\"effectiveDateTime\":\"%s\"}",
                id, source, patientId, name, value, date));
    }
    
    private void ingestCondition(String source, String id, String patientId, String name, String date) {
        FhirIngestRequest req = new FhirIngestRequest();
        req.setResourceType("Condition");
        req.setId(id);
        req.setSourceSystem(source);
        req.setSubject(Map.of("reference", "Patient/" + patientId));
        req.setCode(Map.of("text", name));
        req.setOnsetDateTime(date);
        ingestionService.ingest(req, String.format(
                "{\"resourceType\":\"Condition\",\"id\":\"%s\",\"sourceSystem\":\"%s\",\"subject\":{\"reference\":\"Patient/%s\"},\"code\":{\"text\":\"%s\"},\"onsetDateTime\":\"%s\"}",
                id, source, patientId, name, date));
    }
    
    private void ingestMedication(String source, String id, String patientId, String name, String date) {
        FhirIngestRequest req = new FhirIngestRequest();
        req.setResourceType("MedicationRequest");
        req.setId(id);
        req.setSourceSystem(source);
        req.setSubject(Map.of("reference", "Patient/" + patientId));
        req.setMedicationCodeableConcept(Map.of("text", name));
        req.setAuthoredOn(date);
        ingestionService.ingest(req, String.format(
                "{\"resourceType\":\"MedicationRequest\",\"id\":\"%s\",\"sourceSystem\":\"%s\",\"subject\":{\"reference\":\"Patient/%s\"},\"medicationCodeableConcept\":{\"text\":\"%s\"},\"authoredOn\":\"%s\"}",
                id, source, patientId, name, date));
    }
    
    private void seedTrialData() {
        // Trial 1: Diabetes
        Trial trial1 = new Trial();
        trial1.setTrialCode("TRIAL-DM2-001");
        trial1.setTitle("Type 2 Diabetes Management Study");
        trial1.setDescription("A randomized controlled trial evaluating the efficacy of novel glucose management therapies in patients with Type 2 Diabetes.");
        trial1.setOriginalEligibilityText("Eligible participants must be between 18 and 65 years old, have a diagnosis of Type 2 Diabetes, have HbA1c between 7% and 10%, and have eGFR greater than 30.");
        
        TrialCriterion c1 = new TrialCriterion();
        c1.setTrial(trial1);
        c1.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        c1.setClinicalField("age");
        c1.setOperator("BETWEEN");
        c1.setMinValue(new BigDecimal("18"));
        c1.setMaxValue(new BigDecimal("65"));
        c1.setDescription("Age between 18 and 65 years");
        
        TrialCriterion c2 = new TrialCriterion();
        c2.setTrial(trial1);
        c2.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        c2.setClinicalField("condition");
        c2.setOperator("EQUALS");
        c2.setValue("Type 2 Diabetes");
        c2.setDescription("Diagnosis of Type 2 Diabetes");
        
        TrialCriterion c3 = new TrialCriterion();
        c3.setTrial(trial1);
        c3.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        c3.setClinicalField("hba1c");
        c3.setOperator("BETWEEN");
        c3.setMinValue(new BigDecimal("7"));
        c3.setMaxValue(new BigDecimal("10"));
        c3.setUnit("%");
        c3.setDescription("HbA1c between 7% and 10%");
        
        TrialCriterion c4 = new TrialCriterion();
        c4.setTrial(trial1);
        c4.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        c4.setClinicalField("egfr");
        c4.setOperator("GREATER_THAN");
        c4.setMinValue(new BigDecimal("30"));
        c4.setUnit("mL/min");
        c4.setDescription("eGFR greater than 30 mL/min");
        
        trial1.getCriteria().addAll(List.of(c1, c2, c3, c4));
        trialRepo.save(trial1);

        // Trial 2: Cardiovascular Outcomes
        Trial trial2 = new Trial();
        trial2.setTrialCode("TRIAL-CVD-002");
        trial2.setTitle("Cardiovascular Outcomes Evaluation Study");
        trial2.setDescription("Evaluating long-term cardiovascular risks and management strategies in patients with Hypertension.");
        trial2.setOriginalEligibilityText("Eligible participants must be between 18 and 80 years old and have a diagnosis of Hypertension.");

        TrialCriterion tc1 = new TrialCriterion();
        tc1.setTrial(trial2);
        tc1.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        tc1.setClinicalField("age");
        tc1.setOperator("BETWEEN");
        tc1.setMinValue(new BigDecimal("18"));
        tc1.setMaxValue(new BigDecimal("80"));
        tc1.setDescription("Age between 18 and 80 years");

        TrialCriterion tc2 = new TrialCriterion();
        tc2.setTrial(trial2);
        tc2.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        tc2.setClinicalField("condition");
        tc2.setOperator("EQUALS");
        tc2.setValue("Hypertension");
        tc2.setDescription("Diagnosis of Hypertension");

        trial2.getCriteria().addAll(List.of(tc1, tc2));
        trialRepo.save(trial2);

        // Trial 3: Renal CKD Study
        Trial trial3 = new Trial();
        trial3.setTrialCode("TRIAL-RENAL-003");
        trial3.setTitle("Renal Function Maintenance in Stage 3 CKD");
        trial3.setDescription("Clinical study on slowing progress of Stage 3 Chronic Kidney Disease.");
        trial3.setOriginalEligibilityText("Eligible participants must be between 30 and 75 years old, have a diagnosis of Chronic Kidney Disease, and have eGFR between 15 and 45 mL/min.");

        TrialCriterion tr1 = new TrialCriterion();
        tr1.setTrial(trial3);
        tr1.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        tr1.setClinicalField("age");
        tr1.setOperator("BETWEEN");
        tr1.setMinValue(new BigDecimal("30"));
        tr1.setMaxValue(new BigDecimal("75"));
        tr1.setDescription("Age between 30 and 75 years");

        TrialCriterion tr2 = new TrialCriterion();
        tr2.setTrial(trial3);
        tr2.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        tr2.setClinicalField("condition");
        tr2.setOperator("EQUALS");
        tr2.setValue("Chronic Kidney Disease");
        tr2.setDescription("Diagnosis of Chronic Kidney Disease");

        TrialCriterion tr3 = new TrialCriterion();
        tr3.setTrial(trial3);
        tr3.setCriterionType(TrialCriterion.CriterionType.INCLUSION);
        tr3.setClinicalField("egfr");
        tr3.setOperator("BETWEEN");
        tr3.setMinValue(new BigDecimal("15"));
        tr3.setMaxValue(new BigDecimal("45"));
        tr3.setUnit("mL/min");
        tr3.setDescription("eGFR between 15 and 45 mL/min");

        trial3.getCriteria().addAll(List.of(tr1, tr2, tr3));
        trialRepo.save(trial3);
    }

    private void seedMatchResults() {
        List<Trial> trials = trialRepo.findAll();
        var patients = patientRepo.findAll();
        if (trials.isEmpty() || patients.isEmpty()) return;

        Trial diabetes = trials.stream().filter(t -> "TRIAL-DM2-001".equals(t.getTrialCode())).findFirst().orElseThrow();
        Trial renal = trials.stream().filter(t -> "TRIAL-RENAL-003".equals(t.getTrialCode())).findFirst().orElseThrow();

        // Curated demo outcomes: explainable eligible, ineligible, and review-required cases.
        patients.stream().filter(p -> List.of("John", "Jane", "Maya", "Robert", "Elena", "Marcus").contains(p.getFirstName()))
                .forEach(p -> eligibilityEngine.runMatching(diabetes.getId(), p.getId()));
        patients.stream().filter(p -> List.of("David", "Noah").contains(p.getFirstName()))
                .forEach(p -> eligibilityEngine.runMatching(renal.getId(), p.getId()));
    }
}
