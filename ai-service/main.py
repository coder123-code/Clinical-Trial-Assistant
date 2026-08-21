from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from schemas import ExtractionRequest, ExtractionResponse
from extractor import extract_criteria
import uvicorn

app = FastAPI(
    title="Clinical Trial AI Service",
    description="Extracts structured eligibility criteria from free-text clinical trial descriptions",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5174", "http://localhost:8081"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health():
    return {"status": "healthy", "service": "clinical-trial-ai-service"}

@app.post("/extract-criteria", response_model=ExtractionResponse)
def extract(request: ExtractionRequest):
    if not request.text or len(request.text.strip()) < 10:
        raise HTTPException(status_code=400, detail="Text too short for extraction")
    try:
        return extract_criteria(request.text)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/")
def root():
    return {"message": "Clinical Trial AI Service is running. POST to /extract-criteria"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8091, reload=True)
