package com.clinicaltrial.assistant.repository;
import com.clinicaltrial.assistant.model.StudyVisit; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface StudyVisitRepository extends JpaRepository<StudyVisit,Long>{ List<StudyVisit> findByParticipantTrialIdOrderByScheduledDateAsc(Long trialId); }
