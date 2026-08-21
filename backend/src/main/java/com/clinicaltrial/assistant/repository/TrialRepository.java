package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.Trial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrialRepository extends JpaRepository<Trial, Long> {
    Optional<Trial> findByTrialCode(String trialCode);
    boolean existsByTrialCode(String trialCode);
}
