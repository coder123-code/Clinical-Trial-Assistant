package com.clinicaltrial.assistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String entityType;
    private String entityId;
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    @Column(columnDefinition = "TEXT")
    private String details;
    private String performedBy;
    @Column(length = 64)
    private String previousHash;
    @Column(length = 64)
    private String currentHash;
}
