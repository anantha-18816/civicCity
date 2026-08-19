from fastapi import FastAPI

from app.api.analyze import router as analyze_router

app = FastAPI(title="CivicAI AI Service", version="0.1.0")

app.include_router(analyze_router)


@app.get("/api/health")
def health():
    return {"service": "CivicAI AI Service", "status": "UP", "version": "0.1.0"}