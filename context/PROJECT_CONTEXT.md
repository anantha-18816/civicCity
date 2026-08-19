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
| Mobile directory | STARTED |
| Dashboard directory | STARTED |
| Complaint management | IN PROGRESS |

## Backend

Location:
backend/civicAI-backend

Technology:
Java 17 + Spring Boot 4.1.0

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

Complaint submission.

Target:

POST /api/complaints

Example request:

{
  "userId": 1,
  "issueType": "POTHOLE",
  "title": "Large pothole near college",
  "description": "Deep pothole causing danger to vehicles",
  "latitude": 17.3850,
  "longitude": 78.4867
}

## Current Implementation State

The backend has successfully compiled using:

.\mvnw.cmd clean compile

The Complaint entity/repository/controller/service workflow is currently being built.

The immediate goal is:

Complaint creation -> PostgreSQL/PostGIS -> API response.

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
