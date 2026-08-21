# ClinicalTrialIQ — Supporting Documentation

## Executive Summary

ClinicalTrialIQ is an explainable clinical-trial matching platform. It combines a longitudinal clinical data repository, patient identity resolution, AI-assisted protocol interpretation, deterministic eligibility evaluation, source verification, human review, and audit controls in one workflow.

## Problem and Intended Impact

Clinical trial screening frequently requires coordinators to reconcile patient identifiers, search multiple systems, interpret lengthy protocols, and manually verify source records. ClinicalTrialIQ reduces that burden by automatically assembling evidence and screening a cohort while keeping final control with qualified researchers.

Expected outcomes include faster candidate discovery, fewer missed candidates, clearer handling of incomplete evidence, and improved traceability during research review.

## Implemented Capabilities

### Eligibility criteria extraction

Protocol text and digital PDF text can be submitted for structured inclusion/exclusion extraction. The Python AI service returns schema-validated criteria. A deterministic fallback preserves availability when no external model key is configured.

### Human confirmation and automatic cohort screening

Extracted criteria remain unconfirmed until a researcher selects **Confirm & screen all patients**. The system then evaluates every patient automatically. A single-patient selector is not required for the normal workflow.

### Explainable matching

Each criterion produces PASS, FAIL, MISSING, or REVIEW_REQUIRED. Numeric failures may include a gap-to-pass value and description. Overall eligibility is calculated by deterministic Java logic.

### Source verification

Evidence links retain source system, record ID, event date, and original content. The UI displays formatted FHIR JSON, clinician-note text, or lab-report PDF text and permits downloading the preserved source.

### Patient identity resolution

Source identities are associated with one canonical patient ID. Original source-system IDs remain visible for provenance.

### Governance

Important actions create SHA-256 hash-chained audit entries. `GET /api/audit-logs/verify` walks the chain and identifies any integrity break.

### Post-match trial operations

Matching begins the research lifecycle rather than ending it. The Trial Operations module persists enrollment state, baseline completion, treatment arm, consent version, scheduled and completed visits, dose administration, adherence, laboratory results, efficacy scores, adverse events, protocol deviations, DSMB status, protocol amendments, re-consent requirements, completion, and withdrawal.

The monitored pathway is:

```text
MATCHED → SCREENING → ENROLLED → ACTIVE/MONITORING → COMPLETED or WITHDRAWN
```

Safety or efficacy evidence can lead to investigator review, dose holds, enhanced monitoring, or a formally approved protocol amendment. The system records these decisions; it does not autonomously modify an investigational treatment.

## Safety Model

- AI extracts structure; it does not make the final eligibility decision.
- Missing values are never fabricated.
- Uncertain results are routed to human review.
- Consent is a separate gate from clinical matching.
- Original source provenance is retained.
- Included records are synthetic and contain no real PHI.

## Key API Workflow

```text
POST /api/trials/{id}/extract-criteria
POST /api/trials/{id}/confirm-and-screen
GET  /api/trials/{id}/matches
GET  /api/matches/{id}
GET  /api/patients/{id}/timeline
GET  /api/review-queue
GET  /api/audit-logs/verify
GET  /api/trials/{id}/operations
POST /api/participants/{id}/status
POST /api/study-visits/{id}/complete
POST /api/participants/{id}/adverse-events
```

## Complete Technology Inventory

| Layer | Technologies and purpose |
|---|---|
| Frontend | React 18, Vite, React Router, React Query, Axios, Tailwind CSS, Lucide React, date-fns |
| Main API | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Actuator |
| Persistence | MySQL 8.4, Hibernate/JPA, HikariCP connection pooling, MySQL Workbench administration |
| AI service | Python 3.12, FastAPI, Pydantic, Uvicorn, HTTPX, python-dotenv |
| LLM providers | Current OpenAI-compatible integration; Google Gemini is an appropriate configurable provider for structured clinical-document and protocol extraction |
| PDF tooling | Apache PDFBox for text-layer extraction and standards-compliant PDF generation; Tesseract OCR is recommended for scanned documents |
| Healthcare format | FHIR R4-shaped Patient, Observation, Condition, MedicationRequest, Encounter, and Procedure resources |
| Streaming | Apache Kafka, `clinical-events` topic, Spring Kafka consumer, manual acknowledgement, idempotent source-record constraint |
| Governance | SHA-256 audit hash chaining, source provenance, human criteria confirmation, uncertainty review queue |
| Build/runtime | Maven Wrapper, npm, Node.js 20, Java 21, Python 3.12, MySQL Server, Uvicorn |

### Gemini and LLM positioning

Gemini should be described as a model-provider option, not as the eligibility engine. Whether Gemini or an OpenAI-compatible model is configured, the LLM receives only minimum-necessary text and returns schema-constrained extracted facts or criteria. Pydantic validates the response. A human confirms protocol criteria, and deterministic Java logic makes the reproducible matching calculation.

### Kafka positioning

Kafka provides a scalable asynchronous ingestion path. Producers publish clinical events to `clinical-events`; the Spring consumer normalizes and deduplicates them before persistence. Kafka is disabled in the local default configuration so the application remains runnable without a broker, while the REST ingestion path stays active.

### PDF processing positioning

Digital PDFs are parsed with PDFBox. Preserved lab-report content can be served as a standards-compliant `application/pdf` document and embedded directly in the source viewer. Scanned-image PDFs require an OCR step such as Tesseract before AI fact extraction.

## Database

- Engine: MySQL 8.4
- Host: `localhost`
- Port: `3306`
- Schema: `clinicaltrial`
- JPA mode: `update`, preserving records between application restarts

Main tables include `patients`, `patient_identities`, `clinical_events`, `trials`, `trial_criteria`, `trial_matches`, `criterion_results`, `trial_participants`, `study_visits`, `adverse_events`, `protocol_amendments`, and `audit_logs`.

## MySQL Workbench Access

1. Open MySQL Workbench.
2. Create or open a connection to `localhost:3306`.
3. Use username `root` and your locally configured password.
4. In the **SCHEMAS** panel, select the refresh icon.
5. Expand `clinicaltrial`, then **Tables**.
6. Right-click a table and select **Select Rows — Limit 1000**.

Useful queries:

```sql
USE clinicaltrial;
SHOW TABLES;
SELECT * FROM patients;
SELECT * FROM patient_identities;
SELECT * FROM clinical_events ORDER BY event_timestamp DESC;
SELECT * FROM trials;
SELECT * FROM trial_matches;
SELECT * FROM criterion_results;
SELECT * FROM audit_logs ORDER BY id DESC;
SELECT * FROM trial_participants;
SELECT * FROM study_visits ORDER BY scheduled_date;
SELECT * FROM adverse_events ORDER BY onset_date DESC;
SELECT * FROM protocol_amendments ORDER BY effective_date DESC;
```

## Why a Map Is Not Included Yet

A map becomes clinically and operationally useful only when trial sites have verified coordinates and patients have appropriately consented, privacy-safe geography. The current data model does not contain those facts. Adding a generic map would imply a capability the matching engine cannot support. A future site-proximity module should calculate travel distance, apply site radius constraints, and avoid exposing exact patient addresses.

## Suggested Demonstration Flow

1. Open the dashboard and describe the connected clinical repository.
2. Open a patient profile and show multiple source identities.
3. Open timeline sources in FHIR, note, and lab-report formats.
4. Open a trial and run AI criteria extraction.
5. Review the extracted rules and select **Confirm & screen all patients**.
6. Open one eligible and one potentially eligible explanation.
7. Show missing evidence entering human review.
8. Verify the audit chain in the governance area or API.

## Current Boundaries

This implementation is a research workflow system, not an approved medical device. Production deployment requires formal security controls, access management, privacy review, validation, monitoring, disaster recovery, and integration testing with each participating clinical system.
