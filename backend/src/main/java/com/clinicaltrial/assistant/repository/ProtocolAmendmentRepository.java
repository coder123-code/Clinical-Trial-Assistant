package com.clinicaltrial.assistant.repository;
import com.clinicaltrial.assistant.model.ProtocolAmendment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ProtocolAmendmentRepository extends JpaRepository<ProtocolAmendment,Long>{ List<ProtocolAmendment> findByTrialIdOrderByEffectiveDateDesc(Long trialId); }
