package com.clinicaltrial.assistant.repository;
import com.clinicaltrial.assistant.model.TrialParticipant; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface TrialParticipantRepository extends JpaRepository<TrialParticipant,Long>{ List<TrialParticipant> findByTrialIdOrderByEnrolledDateDesc(Long trialId); long countByTrialId(Long trialId); }
