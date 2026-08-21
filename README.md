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

5. Open `http://localhost:5173/demo` 



All included records are synthetic and must not be used for clinical decisions.

## Kafka

Kafka consumption is implemented for topic `clinical-events`, but is disabled by default. Set `KAFKA_ENABLED=true` through configuration (or change `kafka.enabled`) and provide a broker at `localhost:9092` to demonstrate streaming ingestion. The REST ingestion path remains available at `POST /api/fhir/ingest`.
