package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.PatientIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientIdentityRepository extends JpaRepository<PatientIdentity, Long> {
    Optional<PatientIdentity> findBySourceSystemAndSourcePatientId(String sourceSystem, String sourcePatientId);
    List<PatientIdentity> findByPatientId(Long patientId);
    long countByMatchStatus(PatientIdentity.MatchStatus status);
}
