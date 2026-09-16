# CivicAI Project Context

## Vision

CivicAI is an AI-powered operating system for cities.

It transforms raw citizen complaints into actionable municipal intelligence.

The first production use case is infrastructure and road damage, especially potholes.

## Current Status

| Component | Status |
|---|---|
| Git repository | COMPLETE |
| Project structure | COMPLETE |
| Spring Boot backend | COMPLETE |
| PostgreSQL | COMPLETE |
| PostGIS | COMPLETE |
| Database schema | COMPLETE |
| Health endpoint | COMPLETE |
| AI service skeleton | COMPLETE |
| Docker skeleton | COMPLETE |
| Mobile directory | COMPLETE |
| Dashboard directory | COMPLETE |
| Complaint management | COMPLETE |
| AI analysis | COMPLETE |
| Duplicate detection | COMPLETE |
| Clustering | COMPLETE |
| Priority engine | COMPLETE |
| Department routing | COMPLETE |
| Resolution | COMPLETE |
| Dashboard APIs | COMPLETE |
| Authentication (JWT) | COMPLETE |
| Authorization (roles) | COMPLETE |
| Logging | COMPLETE |
| Monitoring (actuator) | COMPLETE |
| API documentation (swagger) | COMPLETE |
| Docker compose deployment | COMPLETE |
| CI/CD (GitHub Actions) | COMPLETE |
| Production deployment | COMPLETE |

## Backend

Location:
backend/civicAI-backend

Technology:
Java 25 + Spring Boot 4.1.0

Database:
PostgreSQL 18.4

PostGIS:
3.6.2

Port:
8080

Configuration:
backend/civicAI-backend/src/main/resources/application.yaml

## Database

Database:
civicai_db

Tables:
- users
- departments
- complaints
- complaint_ai_analysis
- complaint_clusters
- resolutions

Spatial type:
geometry(Point,4326)

## Working Endpoint

GET /api/health

Expected:

{
  "status": "UP",
  "version": "0.0.1",
  "service": "CivicAI Backend"
}

## Current Feature

The full complaint lifecycle is implemented:

- Citizen reports a complaint (mobile app / API).
- AI service analyzes photos (severity, confidence, image hash).
- Duplicate detection links nearby similar complaints.
- Complaints are clustered and prioritized.
- Routing assigns the right department automatically.
- Officers resolve and verify from the admin dashboard.

## Current Implementation State

All phases of the roadmap are complete. Verification:

- Backend compiles and all 19 integration tests pass.
- `.\mvnw.cmd clean compile`
- Flutter app: `flutter analyze` clean, tests pass.
- AI service: pytest suite passes (CI).
- Full stack runs via `deploy/docker-compose.yml` (db + ai-service + backend + nginx proxy).

## Complaint Lifecycle

SUBMITTED
    ->
AI_ANALYZED
    ->
ASSIGNED
    ->
IN_PROGRESS
    ->
RESOLVED

A complaint may also become REJECTED.

## Long-Term Architecture

Citizen Mobile App
        |
        v
Spring Boot Backend
        |
        +------------------+
        |                  |
        v                  v
PostgreSQL/PostGIS     AI Service
                           |
                           v
                    Computer Vision
                           |
                           v
                    Severity Detection
                           |
                           v
                    Duplicate Detection
        |
        v
Department Routing
        |
        v
Admin Dashboard

## Product Questions CivicAI Should Eventually Answer

- Where are the worst infrastructure problems?
- How many citizens reported the same problem?
- Which complaints are duplicates?
- Which areas need urgent attention?
- Which department is responsible?
- How long does resolution take?
- Has the reported problem actually been fixed?
- What infrastructure problems are likely to appear next?
