# ClinicalTrialIQ

ClinicalTrialIQ is an explainable clinical-trial matching demo. It resolves patient identities across source systems, ingests FHIR-shaped clinical events, converts protocol text into structured criteria, evaluates patients with deterministic rules, and routes uncertainty to a human review queue.

## Run locally

Prerequisites: Java 21, MySQL 8, Python 3.11+, and Node 20+.

1. Create a MySQL database user and set `DB_USERNAME` and `DB_PASSWORD`. The application creates the `clinicaltrial` database automatically when the user has permission.
2. Start the AI service:

   ```powershell
   cd ai-service
   pip install -r requirements.txt
   $env:OPENAI_API_KEY="optional-key"
   python main.py
   ```

   The service uses its deterministic regex fallback when no OpenAI key is set.

3. Start the backend:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

4. Start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

5. Open `http://localhost:5173/demo` and follow the ten-step Demo Journey.

## Judge-ready walkthrough

1. Begin in **Demo Journey**.
2. Open **Patients**, search for **John Smith**, and show four source identities resolving to `P000001`.
3. Open John's timeline and select **View Source** on HbA1c or eGFR to display the preserved FHIR evidence.
4. Open **Clinical Trials** and select `TRIAL-DM2-001`.
5. Create a new trial and choose **Load winning demo protocol**. This is the reliable fallback if a PDF or network call is unavailable.
6. Run matching for John (eligible), Jane/Robert/Elena (not eligible), and Maya (potentially eligible because eGFR is missing).
7. Open a match explanation and verify each criterion result.
8. Open **Review Queue**, select Maya, add clinical notes, and make a decision.
9. Open **Audit Trail** to show the recorded review.

All included records are synthetic and must not be used for clinical decisions.

## Kafka

Kafka consumption is implemented for topic `clinical-events`, but is disabled by default. Set `KAFKA_ENABLED=true` through configuration (or change `kafka.enabled`) and provide a broker at `localhost:9092` to demonstrate streaming ingestion. The REST ingestion path remains available at `POST /api/fhir/ingest`.
