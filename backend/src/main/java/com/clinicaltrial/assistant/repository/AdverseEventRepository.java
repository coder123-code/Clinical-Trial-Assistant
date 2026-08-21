package com.clinicaltrial.assistant.repository;
import com.clinicaltrial.assistant.model.AdverseEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AdverseEventRepository extends JpaRepository<AdverseEvent,Long>{ List<AdverseEvent> findByParticipantTrialIdOrderByOnsetDateDesc(Long trialId); }
