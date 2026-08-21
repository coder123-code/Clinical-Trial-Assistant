package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.ClinicalEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClinicalEventRepository extends JpaRepository<ClinicalEvent, Long> {
    Optional<ClinicalEvent> findBySourceSystemAndSourceRecordId(String sourceSystem, String sourceRecordId);
    List<ClinicalEvent> findByPatientIdOrderByEventTimestampAsc(Long patientId);
    
    @Query("SELECT e FROM ClinicalEvent e WHERE e.patient.id = :patientId " +
           "AND e.eventType = :type ORDER BY e.eventTimestamp DESC")
    List<ClinicalEvent> findByPatientIdAndType(
        @Param("patientId") Long patientId,
        @Param("type") ClinicalEvent.EventType type);
    
    @Query("SELECT e FROM ClinicalEvent e WHERE e.patient.id = :patientId " +
           "AND LOWER(e.displayName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY e.eventTimestamp DESC")
    List<ClinicalEvent> findByPatientIdAndDisplayNameContaining(
        @Param("patientId") Long patientId,
        @Param("name") String name);
    
    long countByPatientId(Long patientId);
    long countByCreatedAtAfter(java.time.LocalDateTime after);
    
    Page<ClinicalEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
