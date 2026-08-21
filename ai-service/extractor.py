import os
import re
import json
from dotenv import load_dotenv
from openai import OpenAI
from schemas import Criterion, ExtractionResponse

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

client = OpenAI(api_key=OPENAI_API_KEY) if OPENAI_API_KEY else None

def extract_criteria(text: str) -> ExtractionResponse:
    if client:
        try:
            return extract_with_llm(text)
        except Exception as e:
            print(f"LLM extraction failed: {e}. Falling back to regex.")
            return extract_with_fallback(text)
    else:
        return extract_with_fallback(text)

def extract_with_llm(text: str) -> ExtractionResponse:
    system_prompt = """You are a clinical trial eligibility criteria extractor. Given free-text eligibility criteria, extract structured JSON. Return ONLY a valid JSON object with a single key "criteria" containing an array. Each criterion object must have: type (INCLUSION or EXCLUSION), field (lowercase snake_case identifier like age, hba1c, egfr, condition, medication, bmi, blood_pressure, creatinine, glucose), operator (BETWEEN, GREATER_THAN, LESS_THAN, EQUALS, NOT_EQUALS, CONTAINS), value (string, optional), min (number, optional), max (number, optional), unit (string, optional), description (string). For BETWEEN, use min and max. For GREATER_THAN, use min. For LESS_THAN, use max."""
    
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": text}
        ],
        response_format={"type": "json_object"}
    )
    
    response_text = response.choices[0].message.content
    data = json.loads(response_text)
    
    criteria_list = []
    for c in data.get("criteria", []):
        criteria_list.append(Criterion(**c))
        
    return ExtractionResponse(
        criteria=criteria_list,
        raw_text=text,
        extraction_method="llm",
        confidence=0.95
    )

def extract_with_fallback(text: str) -> ExtractionResponse:
    criteria_list = []
    
    lines = text.splitlines()
    current_type = "INCLUSION"
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        lower_line = line.lower()
        if ("exclusion" in lower_line and ":" in lower_line) or lower_line.startswith("exclusion"):
            current_type = "EXCLUSION"
            continue
        elif ("inclusion" in lower_line and ":" in lower_line) or lower_line.startswith("inclusion"):
            current_type = "INCLUSION"
            continue
            
        # Age
        age_between = re.search(r'(?:age|aged)?\s*between\s*(\d+)\s*and\s*(\d+)\s*(?:years?|yrs?)', lower_line)
        if age_between:
            criteria_list.append(Criterion(
                type=current_type, field="age", operator="BETWEEN", 
                min=float(age_between.group(1)), max=float(age_between.group(2)), 
                unit="years", description=line
            ))
            continue
            
        age_to = re.search(r'(?:age|aged)?\s*(\d+)\s*(?:to|-)\s*(\d+)\s*(?:years?|yrs?)', lower_line)
        if age_to:
            criteria_list.append(Criterion(
                type=current_type, field="age", operator="BETWEEN", 
                min=float(age_to.group(1)), max=float(age_to.group(2)), 
                unit="years", description=line
            ))
            continue
            
        age_greater = re.search(r'(?:at least|>=|>|greater than|older than)\s*(\d+)\s*(?:years?|yrs?)', lower_line)
        if age_greater:
            criteria_list.append(Criterion(
                type=current_type, field="age", operator="GREATER_THAN", 
                min=float(age_greater.group(1)), unit="years", description=line
            ))
            continue

        age_less = re.search(r'(?:<=|<|less than|younger than)\s*(\d+)\s*(?:years?|yrs?)', lower_line)
        if age_less:
            criteria_list.append(Criterion(
                type=current_type, field="age", operator="LESS_THAN", 
                max=float(age_less.group(1)), unit="years", description=line
            ))
            continue
            
        # HbA1c
        hba1c_between = re.search(r'(?:hba1c|glycated hemoglobin).*?between\s*([\d.]+)\s*(?:%|percent)?\s*and\s*([\d.]+)\s*(?:%|percent)?', lower_line)
        if hba1c_between:
            criteria_list.append(Criterion(
                type=current_type, field="hba1c", operator="BETWEEN",
                min=float(hba1c_between.group(1)), max=float(hba1c_between.group(2)),
                unit="%", description=line
            ))
            continue
            
        hba1c_greater = re.search(r'(?:hba1c|glycated hemoglobin).*(?:>=|>|greater than|at least)\s*([\d.]+)\s*(?:%|percent)?', lower_line)
        if hba1c_greater:
            criteria_list.append(Criterion(
                type=current_type, field="hba1c", operator="GREATER_THAN",
                min=float(hba1c_greater.group(1)), unit="%", description=line
            ))
            continue

        # eGFR
        egfr = re.search(r'(?:egfr|gfr).*(?:>=|>|greater than|at least)\s*([\d.]+)', lower_line)
        if egfr:
            criteria_list.append(Criterion(
                type=current_type, field="egfr", operator="GREATER_THAN",
                min=float(egfr.group(1)), unit="mL/min/1.73m2", description=line
            ))
            continue
            
        # BMI
        bmi_between = re.search(r'bmi.*?between\s*([\d.]+)\s*and\s*([\d.]+)', lower_line)
        if bmi_between:
            criteria_list.append(Criterion(
                type=current_type, field="bmi", operator="BETWEEN",
                min=float(bmi_between.group(1)), max=float(bmi_between.group(2)),
                unit="kg/m2", description=line
            ))
            continue
            
        # Condition
        condition = re.search(r'(?:have|diagnosis of|suffering from)\s+([a-zA-Z0-9\s]+(?:diabetes|cancer|disease|syndrome|disorder)[a-zA-Z0-9\s]*)', lower_line)
        if condition:
            criteria_list.append(Criterion(
                type="INCLUSION", field="condition", operator="EQUALS",
                value=condition.group(1).strip(), description=line
            ))
            continue
            
        # Medication exclusion
        med = re.search(r'(?:currently taking|use of|treatment with)\s+([a-zA-Z0-9\s]+)', lower_line)
        if med and current_type == "EXCLUSION":
            criteria_list.append(Criterion(
                type="EXCLUSION", field="medication", operator="EQUALS",
                value=med.group(1).strip(), description=line
            ))
            continue
            
        # Generic fallback for less than/greater than
        generic_gt = re.search(r'([a-zA-Z0-9\s]+).*(?:>=|>|greater than|at least)\s*([\d.]+)', lower_line)
        if generic_gt:
            field = generic_gt.group(1).strip().split()[-1]
            if len(field) > 2:
                criteria_list.append(Criterion(
                    type=current_type, field=field, operator="GREATER_THAN",
                    min=float(generic_gt.group(2)), description=line
                ))
                continue
                
    return ExtractionResponse(
        criteria=criteria_list,
        raw_text=text,
        extraction_method="fallback",
        confidence=0.75
    )
