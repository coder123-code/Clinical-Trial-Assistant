package com.clinicaltrial.assistant.service;

import com.clinicaltrial.assistant.dto.FhirIngestRequest;
import com.clinicaltrial.assistant.model.ClinicalEvent;
import com.clinicaltrial.assistant.model.Patient;
import com.clinicaltrial.assistant.model.PatientIdentity;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import com.clinicaltrial.assistant.repository.PatientIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {
    private final IdentityResolutionService identityResolutionService;
    private final DeduplicationService deduplicationService;
    private final ClinicalEventRepository clinicalEventRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final AuditService auditService;

    @Transactional
    public String ingest(FhirIngestRequest req, String rawJson) {
        if (req.getResourceType() == null) {
            return "Missing resourceType";
        }

        if ("Patient".equals(req.getResourceType())) {
            return ingestPatient(req, rawJson);
        } else {
            return ingestClinicalEvent(req, rawJson);
        }
    }

    private String ingestPatient(FhirIngestRequest req, String rawJson) {
        String sourceSystem = req.getSourceSystem();
        if (sourceSystem == null && req.getIdentifier() != null && !req.getIdentifier().isEmpty()) {
            sourceSystem = (String) req.getIdentifier().get(0).get("system");
        }
        
        String sourcePatientId = req.getId();
        if (sourcePatientId == null && req.getIdentifier() != null && !req.getIdentifier().isEmpty()) {
            sourcePatientId = (String) req.getIdentifier().get(0).get("value");
        }

        if (sourceSystem == null || sourcePatientId == null) {
            return "Missing sourceSystem or id for Patient";
        }

        String firstName = null;
        String lastName = null;
        if (req.getName() != null && !req.getName().isEmpty()) {
            Map<String, Object> nameMap = req.getName().get(0);
            lastName = (String) nameMap.get("family");
            if (nameMap.get("given") instanceof List) {
                List<?> given = (List<?>) nameMap.get("given");
                if (!given.isEmpty()) {
                    firstName = (String) given.get(0);
                }
            }
        }

        LocalDate dob = null;
        if (req.getBirthDate() != null) {
            try {
                dob = LocalDate.parse(req.getBirthDate());
            } catch (Exception e) {
                log.warn("Could not parse birthDate: {}", req.getBirthDate());
            }
        }

        String phone = null;
        String email = null;
        if (req.getTelecom() != null) {
            for (Map<String, Object> t : req.getTelecom()) {
                String system = (String) t.get("system");
                String value = (String) t.get("value");
                if ("phone".equals(system)) phone = value;
                else if ("email".equals(system)) email = value;
            }
        }

        Patient patient = identityResolutionService.resolvePatient(firstName, lastName, dob, req.getGender(), phone, email, sourceSystem, sourcePatientId);
        return "Ingested Patient " + patient.getCanonicalPatientId();
    }

    private String ingestClinicalEvent(FhirIngestRequest req, String rawJson) {
        String sourceRecordId = req.getId();
        String sourceSystem = req.getSourceSystem();

        if (sourceSystem == null || sourceRecordId == null) {
            return "Missing sourceSystem or id for Clinical Event";
        }

        if (deduplicationService.isDuplicate(sourceSystem, sourceRecordId)) {
            log.debug("Duplicate event skipped: {} {}", sourceSystem, sourceRecordId);
            return "Duplicate skipped";
        }

        String sourcePatientId = null;
        if (req.getSubject() != null && req.getSubject().get("reference") != null) {
            sourcePatientId = ((String) req.getSubject().get("reference")).replace("Patient/", "");
        } else if (req.getPatient() != null && req.getPatient().get("reference") != null) {
            sourcePatientId = ((String) req.getPatient().get("reference")).replace("Patient/", "");
        }

        if (sourcePatientId == null) {
            return "Missing subject reference";
        }

        Optional<PatientIdentity> identityOpt = patientIdentityRepository.findBySourceSystemAndSourcePatientId(sourceSystem, sourcePatientId);
        if (identityOpt.isEmpty()) {
            log.warn("Patient not found for source system {} and ID {}", sourceSystem, sourcePatientId);
            return "Patient not found for source";
        }
        Patient patient = identityOpt.get().getPatient();

        ClinicalEvent event = new ClinicalEvent();
        event.setPatient(patient);
        event.setSourceSystem(sourceSystem);
        event.setSourceRecordId(sourceRecordId);
        event.setRawFhirJson(rawJson);
        event.setSourceFormat("FHIR");

        LocalDateTime eventTimestamp = LocalDateTime.now();
        String dtStr = req.getEffectiveDateTime() != null ? req.getEffectiveDateTime() :
                (req.getOnsetDateTime() != null ? req.getOnsetDateTime() :
                (req.getRecordedDate() != null ? req.getRecordedDate() :
                (req.getAuthoredOn() != null ? req.getAuthoredOn() : req.getPerformedDateTime())));
        
        if (dtStr != null) {
            try {
                if (dtStr.length() == 10) dtStr += "T00:00:00";
                eventTimestamp = LocalDateTime.parse(dtStr);
            } catch (Exception e) {
                log.warn("Could not parse date {}: {}", dtStr, e.getMessage());
            }
        }
        event.setEventTimestamp(eventTimestamp);

        switch (req.getResourceType()) {
            case "Observation":
                event.setEventType(ClinicalEvent.EventType.OBSERVATION);
                if (req.getCode() != null) {
                    event.setDisplayName(extractTextFromConcept(req.getCode()));
                }
                if (req.getValueQuantity() != null) {
                    try {
                        Object valObj = req.getValueQuantity().get("value");
                        if (valObj instanceof Number) {
                            event.setNumericValue(new BigDecimal(valObj.toString()));
                        } else if (valObj instanceof String) {
                            event.setNumericValue(new BigDecimal((String) valObj));
                        }
                    } catch (Exception e) {
                        log.warn("Could not parse valueQuantity: {}", e.getMessage());
                    }
                    event.setUnit((String) req.getValueQuantity().get("unit"));
                    event.setValue(event.getNumericValue() + (event.getUnit() != null ? " " + event.getUnit() : ""));
                } else if (req.getValueString() != null) {
                    event.setValue(req.getValueString());
                }
                break;
            case "Condition":
                event.setEventType(ClinicalEvent.EventType.CONDITION);
                if (req.getCode() != null) {
                    event.setDisplayName(extractTextFromConcept(req.getCode()));
                }
                event.setValue("Active");
                break;
            case "MedicationRequest":
                event.setEventType(ClinicalEvent.EventType.MEDICATION);
                if (req.getMedicationCodeableConcept() != null) {
                    event.setDisplayName(extractTextFromConcept(req.getMedicationCodeableConcept()));
                }
                break;
            case "Procedure":
                event.setEventType(ClinicalEvent.EventType.PROCEDURE);
                if (req.getCode() != null) {
                    event.setDisplayName(extractTextFromConcept(req.getCode()));
                }
                break;
            case "Encounter":
                event.setEventType(ClinicalEvent.EventType.ENCOUNTER);
                if (req.getEncounter() != null) {
                    Object clazz = req.getEncounter().get("class");
                    if (clazz instanceof Map) {
                        event.setDisplayName((String) ((Map<?, ?>) clazz).get("display"));
                    }
                }
                break;
            default:
                event.setEventType(ClinicalEvent.EventType.OBSERVATION);
                event.setDisplayName(req.getResourceType());
        }

        clinicalEventRepository.save(event);
        auditService.log("EVENT_INGESTED", "ClinicalEvent", event.getId().toString(), "Ingested " + req.getResourceType());
        
        return "Ingested " + req.getResourceType();
    }

    private String extractTextFromConcept(Map<String, Object> concept) {
        if (concept.get("text") != null) {
            return (String) concept.get("text");
        }
        if (concept.get("coding") instanceof List) {
            List<?> codingList = (List<?>) concept.get("coding");
            if (!codingList.isEmpty()) {
                Object first = codingList.get(0);
                if (first instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) first;
                    if (map.get("display") != null) {
                        return (String) map.get("display");
                    }
                }
            }
        }
        return "Unknown";
    }
}
