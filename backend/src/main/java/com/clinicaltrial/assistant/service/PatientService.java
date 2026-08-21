package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.PatientDTO;
import com.clinicaltrial.assistant.model.Patient;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import com.clinicaltrial.assistant.repository.PatientIdentityRepository;
import com.clinicaltrial.assistant.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepo;
    private final PatientIdentityRepository patientIdentityRepo;
    private final ClinicalEventRepository clinicalEventRepo;
    
    @Transactional(readOnly = true)
    public Page<PatientDTO> getAllPatients(String search, int page, int size) {
        if (search == null || search.trim().isEmpty()) {
            return patientRepo.findAll(PageRequest.of(page, size)).map(this::toDTO);
        }
        return patientRepo.searchPatients(search, PageRequest.of(page, size)).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PatientDTO getPatientByCanonicalId(String canonicalId) {
        return patientRepo.findByCanonicalPatientId(canonicalId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }
    
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        return patientRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }
    
    private PatientDTO toDTO(Patient p) {
        int age = p.getDateOfBirth() != null ? Period.between(p.getDateOfBirth(), LocalDate.now()).getYears() : 0;
        long eventCount = clinicalEventRepo.countByPatientId(p.getId());
        
        var identities = patientIdentityRepo.findByPatientId(p.getId());
        long sourceCount = identities.stream().map(i -> i.getSourceSystem()).distinct().count();
        
        var identityDtos = identities.stream().map(i -> PatientDTO.IdentityDTO.builder()
                .sourceSystem(i.getSourceSystem())
                .sourcePatientId(i.getSourcePatientId())
                .matchStatus(i.getMatchStatus().name())
                .matchScore(i.getMatchScore())
                .build()).collect(Collectors.toList());
        
        return PatientDTO.builder()
                .id(p.getId())
                .canonicalPatientId(p.getCanonicalPatientId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .phone(p.getPhone())
                .email(p.getEmail())
                .consentStatus(p.getConsentStatus() != null ? p.getConsentStatus().name() : "PENDING")
                .age(age)
                .eventCount(eventCount)
                .sourceCount(sourceCount)
                .createdAt(p.getCreatedAt())
                .identities(identityDtos)
                .build();
    }
}
