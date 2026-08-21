package com.clinicaltrial.assistant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientDTO {
    private Long id;
    private String canonicalPatientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String consentStatus;
    private int age;
    private long eventCount;
    private long sourceCount;
    private LocalDateTime createdAt;
    private List<IdentityDTO> identities;
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class IdentityDTO {
        private String sourceSystem;
        private String sourcePatientId;
        private String matchStatus;
        private Integer matchScore;
    }
}
