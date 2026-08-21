package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.TrialDTO;
import com.clinicaltrial.assistant.model.Trial;
import com.clinicaltrial.assistant.model.TrialCriterion;
import com.clinicaltrial.assistant.repository.TrialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrialService {
    private final TrialRepository trialRepository;
    private final AiServiceClient aiServiceClient;
    private final AuditService auditService;

    @Transactional
    public TrialDTO createTrial(TrialDTO dto) {
        Trial trial = new Trial();
        trial.setTitle(dto.getTitle());
        trial.setDescription(dto.getDescription());
        trial.setOriginalEligibilityText(dto.getOriginalEligibilityText());
        
        String code = dto.getTrialCode();
        if (code == null || code.trim().isEmpty()) {
            code = "TRIAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        trial.setTrialCode(code);
        
        trial = trialRepository.save(trial);
        auditService.log("TRIAL_CREATED", "Trial", trial.getId().toString(), "Created trial " + trial.getTrialCode());
        return toDTO(trial);
    }

    @Transactional(readOnly = true)
    public List<TrialDTO> getAll() {
        return trialRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrialDTO getById(Long id) {
        return trialRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Trial not found"));
    }

    @Transactional
    public TrialDTO extractAndSaveCriteria(Long trialId, String eligibilityText) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new RuntimeException("Trial not found"));
        
        trial.setOriginalEligibilityText(eligibilityText);
        trial.getCriteria().clear();
        
        Map<String, Object> response = aiServiceClient.extractCriteria(eligibilityText);
        
        if (response != null && response.containsKey("criteria")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> criteriaList = (List<Map<String, Object>>) response.get("criteria");
            
            for (Map<String, Object> cMap : criteriaList) {
                TrialCriterion c = new TrialCriterion();
                c.setTrial(trial);
                c.setCriterionType(TrialCriterion.CriterionType.valueOf(((String) cMap.getOrDefault("type", "INCLUSION")).toUpperCase()));
                c.setClinicalField((String) cMap.get("field"));
                c.setOperator((String) cMap.get("operator"));
                c.setValue((String) cMap.get("value"));
                if (cMap.get("min") != null) c.setMinValue(new BigDecimal(cMap.get("min").toString()));
                if (cMap.get("max") != null) c.setMaxValue(new BigDecimal(cMap.get("max").toString()));
                c.setUnit((String) cMap.get("unit"));
                c.setDescription((String) cMap.get("description"));
                
                trial.getCriteria().add(c);
            }
        }
        
        trial = trialRepository.save(trial);
        auditService.log("CRITERIA_EXTRACTED", "Trial", trial.getId().toString(), "Extracted criteria using AI");
        return toDTO(trial);
    }

    private TrialDTO toDTO(Trial t) {
        List<TrialDTO.CriterionDTO> critDtos = t.getCriteria().stream().map(c -> TrialDTO.CriterionDTO.builder()
                .id(c.getId())
                .criterionType(c.getCriterionType().name())
                .clinicalField(c.getClinicalField())
                .operator(c.getOperator())
                .value(c.getValue())
                .minValue(c.getMinValue())
                .maxValue(c.getMaxValue())
                .unit(c.getUnit())
                .description(c.getDescription())
                .reviewedByHuman(c.isReviewedByHuman())
                .build()).collect(Collectors.toList());

        return TrialDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .trialCode(t.getTrialCode())
                .description(t.getDescription())
                .originalEligibilityText(t.getOriginalEligibilityText())
                .createdAt(t.getCreatedAt())
                .criteria(critDtos)
                .matchCount(0) // Could optimize to fetch match count
                .build();
    }

    @Transactional
    public TrialDTO confirmCriteria(Long trialId) {
        Trial trial = trialRepository.findById(trialId).orElseThrow(() -> new RuntimeException("Trial not found"));
        trial.getCriteria().forEach(c -> c.setReviewedByHuman(true));
        trial = trialRepository.save(trial);
        auditService.log("CRITERIA_CONFIRMED", "Trial", trial.getId().toString(), "Extracted criteria approved by human reviewer");
        return toDTO(trial);
    }
}
