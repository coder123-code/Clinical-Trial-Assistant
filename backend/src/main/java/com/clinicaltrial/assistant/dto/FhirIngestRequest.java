package com.clinicaltrial.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FhirIngestRequest {
    private String resourceType;
    private String id;
    private Map<String, Object> subject;
    private Map<String, Object> patient;
    private Map<String, Object> code;
    private Map<String, Object> valueQuantity;
    private String valueString;
    private String effectiveDateTime;
    private String onsetDateTime;
    private String recordedDate;
    private String authoredOn;
    private String performedDateTime;
    private String status;
    private Map<String, Object> medicationCodeableConcept;
    private Map<String, Object> encounter;
    private String sourceSystem;
    // FHIR Patient fields
    private java.util.List<Map<String, Object>> name;
    private String birthDate;
    private String gender;
    private java.util.List<Map<String, Object>> telecom;
    private java.util.List<Map<String, Object>> identifier;
}
