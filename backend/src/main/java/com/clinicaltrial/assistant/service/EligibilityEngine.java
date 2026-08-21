package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.MatchResultDTO;
import com.clinicaltrial.assistant.dto.MatchResultDTO.CriterionResultDTO;
import com.clinicaltrial.assistant.dto.ReviewQueueDTO;
import com.clinicaltrial.assistant.model.*;
import com.clinicaltrial.assistant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EligibilityEngine {

    private final TrialRepository trialRepo;
    private final PatientRepository patientRepo;
    private final ClinicalEventRepository eventRepo;
    private final TrialMatchRepository matchRepo;
    private final AuditService auditService;

    @Transactional
    public MatchResultDTO runMatching(Long trialId, Long patientId) {
        Trial trial = trialRepo.findById(trialId)
                .orElseThrow(() -> new RuntimeException("Trial not found"));
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        TrialMatch match = matchRepo.findByTrialIdAndPatientId(trialId, patientId)
                .orElse(new TrialMatch());
        match.setTrial(trial);
        match.setPatient(patient);
        
        match.getCriterionResults().clear();

        boolean anyExclusionFail = false; // if an exclusion matches, patient is NOT_ELIGIBLE
        boolean anyInclusionFail = false;
        boolean anyInclusionMissing = false;
        boolean anyInclusionPass = false;

        for (TrialCriterion criterion : trial.getCriteria()) {
            CriterionResult cr = evaluateCriterion(criterion, patient);
            cr.setTrialMatch(match);
            match.getCriterionResults().add(cr);

            if (criterion.getCriterionType() == TrialCriterion.CriterionType.EXCLUSION) {
                if (cr.getStatus() == CriterionResult.ResultStatus.PASS) {
                    anyExclusionFail = true; 
                }
            } else {
                if (cr.getStatus() == CriterionResult.ResultStatus.PASS) {
                    anyInclusionPass = true;
                } else if (cr.getStatus() == CriterionResult.ResultStatus.FAIL) {
                    anyInclusionFail = true;
                } else if (cr.getStatus() == CriterionResult.ResultStatus.MISSING) {
                    anyInclusionMissing = true;
                }
            }
        }

        if (anyExclusionFail || anyInclusionFail) {
            match.setStatus(TrialMatch.MatchStatus.NOT_ELIGIBLE);
        } else if (anyInclusionMissing) {
            match.setStatus(anyInclusionPass ? TrialMatch.MatchStatus.POTENTIALLY_ELIGIBLE : TrialMatch.MatchStatus.NEEDS_REVIEW);
        } else {
            match.setStatus(TrialMatch.MatchStatus.ELIGIBLE);
        }

        TrialMatch savedMatch = matchRepo.save(match);
        auditService.log("TRIAL_MATCH_RUN", "TrialMatch", savedMatch.getId().toString(), "Match status: " + savedMatch.getStatus());
        
        return toDto(savedMatch);
    }

    @Transactional
    public List<MatchResultDTO> runCohortMatching(Long trialId) {
        Trial trial = trialRepo.findById(trialId).orElseThrow(() -> new RuntimeException("Trial not found"));
        if (trial.getCriteria().isEmpty()) throw new RuntimeException("Extract and confirm criteria before cohort screening");
        if (trial.getCriteria().stream().anyMatch(c -> !c.isReviewedByHuman())) throw new RuntimeException("Human confirmation is required before cohort screening");
        List<MatchResultDTO> results = patientRepo.findAll().stream().map(p -> runMatching(trialId, p.getId())).toList();
        auditService.log("COHORT_SCREEN_COMPLETED", "Trial", trialId.toString(), "Automatically screened " + results.size() + " patients");
        return results;
    }

    private CriterionResult evaluateCriterion(TrialCriterion criterion, Patient patient) {
        CriterionResult cr = new CriterionResult();
        cr.setCriterion(criterion);

        if ("age".equalsIgnoreCase(criterion.getClinicalField())) {
            int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
            cr.setPatientValue(String.valueOf(age));
            cr.setExplanation("Patient age: " + age + " years");
            
            boolean passed = evaluateNumeric(new java.math.BigDecimal(age), criterion);
            cr.setStatus(passed ? CriterionResult.ResultStatus.PASS : CriterionResult.ResultStatus.FAIL);
            if (!passed) applyGap(cr, new BigDecimal(age), criterion);
            return cr;
        }

        if ("condition".equalsIgnoreCase(criterion.getClinicalField()) || "diagnosis".equalsIgnoreCase(criterion.getClinicalField())) {
            List<ClinicalEvent> conditions = eventRepo.findByPatientIdAndType(patient.getId(), ClinicalEvent.EventType.CONDITION);
            for (ClinicalEvent event : conditions) {
                if (event.getDisplayName() != null && criterion.getValue() != null && 
                    event.getDisplayName().toLowerCase().contains(criterion.getValue().toLowerCase())) {
                    cr.setClinicalEvent(event);
                    cr.setPatientValue(event.getDisplayName());
                    cr.setStatus(CriterionResult.ResultStatus.PASS);
                    cr.setExplanation("Found matching condition");
                    return cr;
                }
            }
            cr.setStatus(CriterionResult.ResultStatus.MISSING);
            cr.setExplanation("No matching condition found");
            return cr;
        }

        if ("medication".equalsIgnoreCase(criterion.getClinicalField())) {
            List<ClinicalEvent> medications = eventRepo.findByPatientIdAndType(patient.getId(), ClinicalEvent.EventType.MEDICATION);
            ClinicalEvent medication = medications.stream()
                    .filter(e -> e.getDisplayName() != null && criterion.getValue() != null
                            && e.getDisplayName().toLowerCase().contains(criterion.getValue().toLowerCase()))
                    .findFirst().orElse(null);
            cr.setClinicalEvent(medication);
            cr.setPatientValue(medication != null ? medication.getDisplayName() : null);
            cr.setStatus(medication != null ? CriterionResult.ResultStatus.PASS : CriterionResult.ResultStatus.MISSING);
            cr.setExplanation(medication != null ? "Medication found in longitudinal record" : "No matching medication found");
            return cr;
        }

        // Observation
        List<ClinicalEvent> observations = eventRepo.findByPatientIdAndType(patient.getId(), ClinicalEvent.EventType.OBSERVATION);
        ClinicalEvent foundEvent = null;
        for (ClinicalEvent event : observations) {
            if (event.getDisplayName() != null && event.getDisplayName().toLowerCase().contains(criterion.getClinicalField().toLowerCase())) {
                foundEvent = event;
                break; // get first (most recent due to order)
            }
        }

        if (foundEvent == null) {
            cr.setStatus(CriterionResult.ResultStatus.MISSING);
            cr.setExplanation("No observation found for field");
            return cr;
        }

        cr.setClinicalEvent(foundEvent);
        if (foundEvent.getNumericValue() != null) {
            cr.setPatientValue(foundEvent.getNumericValue().toPlainString());
            boolean passed = evaluateNumeric(foundEvent.getNumericValue(), criterion);
            cr.setStatus(passed ? CriterionResult.ResultStatus.PASS : CriterionResult.ResultStatus.FAIL);
            cr.setExplanation(passed ? "Value meets criterion" : "Value does not meet criterion");
            if (!passed) applyGap(cr, foundEvent.getNumericValue(), criterion);
        } else {
            cr.setPatientValue(foundEvent.getValue());
            if ("EQUALS".equalsIgnoreCase(criterion.getOperator())) {
                boolean passed = foundEvent.getValue() != null && foundEvent.getValue().equalsIgnoreCase(criterion.getValue());
                cr.setStatus(passed ? CriterionResult.ResultStatus.PASS : CriterionResult.ResultStatus.FAIL);
                cr.setExplanation(passed ? "Value matches" : "Value does not match");
            } else {
                cr.setStatus(CriterionResult.ResultStatus.REVIEW_REQUIRED);
                cr.setExplanation("Non-numeric value needs manual review");
            }
        }
        
        return cr;
    }

    private boolean evaluateNumeric(java.math.BigDecimal value, TrialCriterion criterion) {
        if ("BETWEEN".equalsIgnoreCase(criterion.getOperator()) && criterion.getMinValue() != null && criterion.getMaxValue() != null) {
            return value.compareTo(criterion.getMinValue()) >= 0 && value.compareTo(criterion.getMaxValue()) <= 0;
        } else if ("GREATER_THAN".equalsIgnoreCase(criterion.getOperator())) {
            java.math.BigDecimal target = criterion.getMinValue() != null ? criterion.getMinValue() : 
                (criterion.getValue() != null ? new java.math.BigDecimal(criterion.getValue()) : null);
            return target != null && value.compareTo(target) > 0;
        } else if ("LESS_THAN".equalsIgnoreCase(criterion.getOperator())) {
            java.math.BigDecimal target = criterion.getMaxValue() != null ? criterion.getMaxValue() : 
                (criterion.getValue() != null ? new java.math.BigDecimal(criterion.getValue()) : null);
            return target != null && value.compareTo(target) < 0;
        } else if ("EQUALS".equalsIgnoreCase(criterion.getOperator())) {
            java.math.BigDecimal target = criterion.getValue() != null ? new java.math.BigDecimal(criterion.getValue()) : null;
            return target != null && value.compareTo(target) == 0;
        }
        return false;
    }

    private void applyGap(CriterionResult result, BigDecimal patientValue, TrialCriterion criterion) {
        BigDecimal gap = null; String target = null;
        if (criterion.getMinValue() != null && patientValue.compareTo(criterion.getMinValue()) < 0) { gap = criterion.getMinValue().subtract(patientValue); target = criterion.getMinValue().toPlainString(); }
        else if (criterion.getMaxValue() != null && patientValue.compareTo(criterion.getMaxValue()) > 0) { gap = patientValue.subtract(criterion.getMaxValue()); target = criterion.getMaxValue().toPlainString(); }
        if (gap != null) { result.setGapToPass(gap); result.setGapDescription(criterion.getClinicalField() + " is " + gap.stripTrailingZeros().toPlainString() + " from the qualifying threshold (" + patientValue.stripTrailingZeros().toPlainString() + " vs " + target + ")"); }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> getGapAnalysis(Long trialId) {
        List<TrialMatch> matches = matchRepo.findByTrialId(trialId).stream().filter(m -> m.getStatus() == TrialMatch.MatchStatus.NOT_ELIGIBLE).toList();
        long one = matches.stream().filter(m -> m.getCriterionResults().stream().filter(r -> r.getStatus() == CriterionResult.ResultStatus.FAIL).count() == 1).count();
        long multiple = matches.stream().filter(m -> m.getCriterionResults().stream().filter(r -> r.getStatus() == CriterionResult.ResultStatus.FAIL).count() > 1).count();
        var closest = matches.stream().flatMap(m -> m.getCriterionResults().stream()).filter(r -> r.getGapToPass() != null).min(java.util.Comparator.comparing(CriterionResult::getGapToPass));
        Map<String,Object> out = new LinkedHashMap<>(); out.put("notEligiblePatients", matches.size()); out.put("blockedByOneCriterion", one); out.put("blockedByMultipleCriteria", multiple); out.put("closestCriterion", closest.map(r -> r.getCriterion().getDescription()).orElse(null)); out.put("smallestGap", closest.map(r -> r.getGapToPass().stripTrailingZeros().toPlainString()).orElse(null)); return out;
    }

    @Transactional(readOnly = true)
    public MatchResultDTO getMatchResult(Long matchId) {
        return matchRepo.findById(matchId).map(this::toDto).orElseThrow(() -> new RuntimeException("Match not found"));
    }

    @Transactional(readOnly = true)
    public List<MatchResultDTO> getMatchesByPatient(Long patientId) {
        return matchRepo.findByPatientId(patientId).stream().map(this::toDto).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MatchResultDTO> getMatchesByTrial(Long trialId) {
        return matchRepo.findByTrialId(trialId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public MatchResultDTO resolveReview(Long matchId, String notes, String decision) {
        TrialMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        match.setStatus("ELIGIBLE".equalsIgnoreCase(decision)
                ? TrialMatch.MatchStatus.ELIGIBLE : TrialMatch.MatchStatus.NOT_ELIGIBLE);
        match.setReviewNotes(notes + " | Decision: " + decision);
        match = matchRepo.save(match);
        auditService.log("MATCH_REVIEWED", "TrialMatch", match.getId().toString(), "Manual review decision: " + decision);
        return toDto(match);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewQueueDTO> getReviewQueue() {
        List<TrialMatch> matches = matchRepo.findByStatus(TrialMatch.MatchStatus.NEEDS_REVIEW);
        matches.addAll(matchRepo.findByStatus(TrialMatch.MatchStatus.POTENTIALLY_ELIGIBLE));
        
        return matches.stream().map(m -> {
            long missing = m.getCriterionResults().stream().filter(cr -> cr.getStatus() == CriterionResult.ResultStatus.MISSING).count();
            long failed = m.getCriterionResults().stream().filter(cr -> cr.getStatus() == CriterionResult.ResultStatus.FAIL).count();
            
            return ReviewQueueDTO.builder()
                .matchId(m.getId())
                .patientName(m.getPatient().getFirstName() + " " + m.getPatient().getLastName())
                .canonicalPatientId(m.getPatient().getCanonicalPatientId())
                .trialTitle(m.getTrial().getTitle())
                .trialCode(m.getTrial().getTrialCode())
                .currentStatus(m.getStatus().name())
                .reviewNotes(m.getReviewNotes())
                .matchedAt(m.getMatchedAt())
                .missingCriteria(missing)
                .failedCriteria(failed)
                .totalCriteria(m.getCriterionResults().size())
                .build();
        }).collect(Collectors.toList());
    }

    private MatchResultDTO toDto(TrialMatch m) {
        List<CriterionResultDTO> crDtos = m.getCriterionResults().stream().map(cr -> {
            CriterionResultDTO dto = CriterionResultDTO.builder()
                .id(cr.getId())
                .criterionType(cr.getCriterion().getCriterionType().name())
                .clinicalField(cr.getCriterion().getClinicalField())
                .operator(cr.getCriterion().getOperator())
                .criterionValue(cr.getCriterion().getValue())
                .criterionMin(cr.getCriterion().getMinValue() != null ? cr.getCriterion().getMinValue().toPlainString() : null)
                .criterionMax(cr.getCriterion().getMaxValue() != null ? cr.getCriterion().getMaxValue().toPlainString() : null)
                .unit(cr.getCriterion().getUnit())
                .criterionDescription(cr.getCriterion().getDescription())
                .status(cr.getStatus() != null ? cr.getStatus().name() : null)
                .patientValue(cr.getPatientValue())
                .explanation(cr.getExplanation())
                .gapToPass(cr.getGapToPass() != null ? cr.getGapToPass().toPlainString() : null)
                .gapDescription(cr.getGapDescription())
                .build();
            if (cr.getClinicalEvent() != null) {
                dto.setSourceSystem(cr.getClinicalEvent().getSourceSystem());
                dto.setSourceEventId(cr.getClinicalEvent().getId());
                dto.setSourceRecordId(cr.getClinicalEvent().getSourceRecordId());
                dto.setEventTimestamp(cr.getClinicalEvent().getEventTimestamp());
                dto.setRawFhirJson(cr.getClinicalEvent().getRawFhirJson());
                dto.setSourceFormat(cr.getClinicalEvent().getSourceFormat());
                dto.setRawSourceText(cr.getClinicalEvent().getRawSourceText());
                dto.setSourceDocumentName(cr.getClinicalEvent().getSourceDocumentName());
            }
            return dto;
        }).collect(Collectors.toList());

        return MatchResultDTO.builder()
            .id(m.getId())
            .status(m.getStatus().name())
            .matchedAt(m.getMatchedAt())
            .reviewNotes(m.getReviewNotes())
            .trialId(m.getTrial().getId())
            .trialTitle(m.getTrial().getTitle())
            .trialCode(m.getTrial().getTrialCode())
            .patientId(m.getPatient().getId())
            .patientName(m.getPatient().getFirstName() + " " + m.getPatient().getLastName())
            .canonicalPatientId(m.getPatient().getCanonicalPatientId())
            .criterionResults(crDtos)
            .build();
    }
}
