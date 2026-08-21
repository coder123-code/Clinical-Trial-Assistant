package com.clinicaltrial.assistant.repository;

import com.clinicaltrial.assistant.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByCanonicalPatientId(String canonicalPatientId);
    
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) = LOWER(:firstName) AND " +
           "LOWER(p.lastName) = LOWER(:lastName) AND " +
           "p.dateOfBirth = :dob")
    Optional<Patient> findByNameAndDob(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("dob") LocalDate dob);
    
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "p.canonicalPatientId LIKE CONCAT('%', :search, '%')")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);
    
    long countByCreatedAtAfter(java.time.LocalDateTime after);
}
