package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.TrialMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TrialMatchRepository extends JpaRepository<TrialMatch, Long> {
    List<TrialMatch> findByPatientId(Long patientId);
    List<TrialMatch> findByTrialId(Long trialId);
    Optional<TrialMatch> findByTrialIdAndPatientId(Long trialId, Long patientId);
    List<TrialMatch> findByStatus(TrialMatch.MatchStatus status);
    
    @Query("SELECT COUNT(m) FROM TrialMatch m WHERE m.status IN ('POTENTIALLY_ELIGIBLE', 'NEEDS_REVIEW')")
    long countPotentialMatches();
    
    @Query("SELECT COUNT(m) FROM TrialMatch m WHERE m.status = 'NEEDS_REVIEW'")
    long countNeedsReview();
}
