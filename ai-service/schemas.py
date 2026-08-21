from pydantic import BaseModel, field_validator
from typing import Optional, List, Literal

class Criterion(BaseModel):
    type: Literal["INCLUSION", "EXCLUSION"]
    field: str
    operator: Literal["BETWEEN", "GREATER_THAN", "LESS_THAN", "EQUALS", "NOT_EQUALS", "CONTAINS"]
    value: Optional[str] = None
    min: Optional[float] = None
    max: Optional[float] = None
    unit: Optional[str] = None
    description: Optional[str] = None
    
    @field_validator('field')
    @classmethod
    def normalize_field(cls, v: str) -> str:
        return v.lower().strip().replace(' ', '_').replace('-', '_')

class ExtractionRequest(BaseModel):
    text: str
    
class ExtractionResponse(BaseModel):
    criteria: List[Criterion]
    raw_text: str
    extraction_method: str  # "llm" or "fallback"
    confidence: float
