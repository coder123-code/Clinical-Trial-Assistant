package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
    java.util.Optional<AuditLog> findTopByOrderByIdDesc();
    List<AuditLog> findAllByOrderByIdAsc();
}
