package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.model.Patient;
import com.clinicaltrial.assistant.model.PatientIdentity;
import com.clinicaltrial.assistant.repository.PatientIdentityRepository;
import com.clinicaltrial.assistant.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityResolutionService {

    private final PatientRepository patientRepo;
    private final PatientIdentityRepository patientIdentityRepo;
    private final AuditService auditService;

    @Transactional
    public Patient resolvePatient(String firstName, String lastName, LocalDate dob, String gender, String phone, String email, String sourceSystem, String sourcePatientId) {
        log.info("Resolving patient from {} with source ID {}", sourceSystem, sourcePatientId);

        Optional<PatientIdentity> existingIdentity = patientIdentityRepo.findBySourceSystemAndSourcePatientId(sourceSystem, sourcePatientId);
        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getPatient();
        }

        Optional<Patient> existingPatient = patientRepo.findByNameAndDob(firstName, lastName, dob);
        
        if (existingPatient.isPresent()) {
            Patient patient = existingPatient.get();
            log.info("Found exact match for patient {} {}, creating confirmed identity", firstName, lastName);
            
            PatientIdentity identity = PatientIdentity.builder()
                    .patient(patient)
                    .sourceSystem(sourceSystem)
                    .sourcePatientId(sourcePatientId)
                    .matchStatus(PatientIdentity.MatchStatus.CONFIRMED)
                    .build();
            patientIdentityRepo.save(identity);
            auditService.log("IDENTITY_LINKED", "Patient", patient.getId().toString(), "Linked source " + sourceSystem + " " + sourcePatientId);
            return patient;
        }

        log.info("No match found for {} {}, creating new patient", firstName, lastName);
        String canonicalId = generateCanonicalId();
        
        Patient newPatient = Patient.builder()
                .canonicalPatientId(canonicalId)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dob)
                .gender(gender)
                .phone(phone)
                .email(email)
                .build();
        newPatient = patientRepo.save(newPatient);

        PatientIdentity identity = PatientIdentity.builder()
                .patient(newPatient)
                .sourceSystem(sourceSystem)
                .sourcePatientId(sourcePatientId)
                .matchStatus(PatientIdentity.MatchStatus.NEW_PATIENT)
                .build();
        patientIdentityRepo.save(identity);
        
        auditService.log("PATIENT_CREATED", "Patient", newPatient.getId().toString(), "Created new patient with canonical ID " + canonicalId);
        
        return newPatient;
    }
    
    private String generateCanonicalId() {
        long count = patientRepo.count();
        return String.format("P%06d", count + 1);
    }
}
