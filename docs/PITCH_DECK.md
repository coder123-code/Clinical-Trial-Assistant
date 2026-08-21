# ClinicalTrialIQ — Pitch Deck Content

## Slide 1 — Title

**ClinicalTrialIQ**  
Explainable AI for faster, safer clinical-trial recruitment

**One-line pitch:** ClinicalTrialIQ unifies fragmented patient records, turns complex protocols into reviewable rules, and automatically identifies trial candidates with traceable evidence and human oversight.

## Slide 2 — The Problem

- Trial recruitment is slowed by manual chart review and complex eligibility criteria.
- A single patient may have disconnected identities across hospitals, laboratories, and clinics.
- Relevant evidence is split across FHIR records, clinician notes, and PDF reports.
- Missing evidence is easily confused with negative evidence.
- Research teams need speed, but also provenance, compliance, and explainability.

**Impact:** Delayed recruitment increases trial cost, delays therapies, and leaves eligible patients undiscovered.

## Slide 3 — Our Solution

ClinicalTrialIQ provides one governed workflow:

1. Ingest structured and document-based clinical evidence.
2. Resolve multiple source identities into a canonical patient record.
3. Use AI to extract inclusion and exclusion criteria from protocol text or PDF.
4. Require human confirmation of AI-extracted criteria.
5. Automatically screen the entire patient cohort with deterministic rules.
6. Explain every result and link it to the exact source record.
7. Route missing or uncertain evidence to a human review queue.

## Slide 4 — Product Experience

- Clinical intelligence dashboard with recruitment and review metrics
- Searchable canonical patient registry with profile images
- Longitudinal timeline across connected systems
- Source-document viewer for FHIR JSON, doctor notes, and lab-report PDFs
- AI-assisted protocol workspace
- Automatic cohort screening after human criteria confirmation
- Per-criterion PASS, FAIL, MISSING, or REVIEW_REQUIRED explanations
- Review queue and tamper-evident audit trail

## Slide 5 — How the Intelligence Works

**AI assists; rules decide; humans govern.**

- AI converts unstructured protocol language into structured candidate rules.
- Pydantic validation constrains AI output to an explicit schema.
- A deterministic Java engine compares confirmed criteria with patient evidence.
- AI never makes the final eligibility decision.
- Missing clinical values remain missing and are never invented.
- Every criterion result retains its source system, record ID, timestamp, and original content.

## Slide 6 — Architecture

```text
FHIR / Notes / PDF
        │
        ▼
Spring Boot ingestion → identity resolution → MySQL clinical repository
        │                                      │
        │                                      ▼
        └──────── FastAPI AI extraction → confirmed trial criteria
                                               │
                                               ▼
                                  deterministic cohort screening
                                               │
                                               ▼
                                React UI / review / audit / evidence
```

**Technology and tools:**

- **AI/LLM layer:** an OpenAI-compatible provider is currently wired; Google Gemini can be selected as an enterprise model provider through the same schema-constrained extraction boundary.
- **AI service:** Python 3.12, FastAPI, Pydantic validation, HTTPX, and deterministic regex fallback.
- **Document intelligence:** Apache PDFBox parses digital protocol PDFs and produces valid source-document PDFs. OCR tools such as Tesseract are a production extension for scanned pages.
- **Backend:** Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation, Hibernate, and HikariCP.
- **Clinical interoperability:** FHIR R4 resource concepts including Patient, Observation, Condition, MedicationRequest, Encounter, and Procedure.
- **Data:** MySQL 8 with MySQL Workbench for inspection and administration.
- **Streaming:** Apache Kafka, with the `clinical-events` topic and consumer implementation available for asynchronous ingestion.
- **Frontend:** React, Vite, React Query, Axios, Tailwind CSS, Lucide icons, and responsive evidence viewers.
- **Developer tooling:** Maven Wrapper, npm, Uvicorn, GitHub Actions structure, and REST health/audit endpoints.

## Slide 7 — Innovation

- **Two-dimensional fragmentation resolution:** unifies both different patient identities and different data formats.
- **Human-confirmed AI:** extraction is accelerated without giving AI decision authority.
- **Evidence-first matching:** every result links to its originating clinical artifact.
- **Gap analysis:** shows how far a failed numeric result is from a qualifying threshold.
- **Safe uncertainty:** incomplete evidence becomes a review task, not a false conclusion.
- **Tamper-evident governance:** SHA-256 chained audit entries can be independently verified.

## Slide 8 — Business Value

- Reduces repetitive manual pre-screening work
- Expands the discoverable candidate pool across fragmented systems
- Gives coordinators a prioritized, explainable review workflow
- Improves source-data verification readiness
- Creates reusable infrastructure across multiple trials
- Shortens time from protocol availability to first candidate list

**Primary users:** trial sponsors, CROs, academic medical centers, research networks, and site coordinators.

## Slide 9 — Safety, Privacy, and Compliance

- Minimum-necessary data sent to the AI service
- Human review before extracted criteria become operational
- Deterministic final eligibility logic
- Consent tracked separately from clinical eligibility
- Complete source provenance retained
- Uncertainty escalated instead of auto-approved
- Tamper-evident audit verification endpoint
- Current included records are synthetic and intended for research workflow evaluation

## Slide 10 — Validation Scenario

- Eleven patient profiles across multiple clinical systems
- Thirty-three longitudinal clinical events
- Three active trial protocols
- One canonical patient connected to four source identities
- Evidence represented as FHIR, a clinician note, and a digital lab report
- Automatic cohort screening categorizes eligible, potentially eligible, and non-eligible patients
- Every decision can be traced back to its supporting record
- A 180-day active study demonstrates longitudinal operations after matching
- Participant treatment arms, visit schedules, dosing, adherence, safety events, efficacy signals, deviations, and protocol amendments are monitored

## Slide 11 — Roadmap

**Near term:** production identity-review workflow, trial-site geography, role-based access, and configurable electronic data capture integrations.  
**Next:** FHIR server connectors, enabled Kafka pipelines, protocol retrieval over long documents, and configurable terminology services.  
**Scale:** multi-tenant research networks, site-level analytics, and privacy-preserving cross-organization candidate discovery.

## Slide 12 — Closing

**ClinicalTrialIQ turns disconnected data into governed recruitment intelligence.**

It helps research teams find the right evidence, for the right patient, for the right trial—without sacrificing explainability or human accountability.
