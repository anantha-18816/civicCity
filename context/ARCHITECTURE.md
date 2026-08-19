# CivicAI Architecture

## High-Level Architecture

                    +------------------+
                    | Citizen Flutter  |
                    |      App         |
                    +--------+---------+
                             |
                             | REST
                             v
                    +------------------+
                    | Spring Boot API  |
                    |                  |
                    | Controllers      |
                    | Services         |
                    | Repositories     |
                    +--------+---------+
                             |
                +------------+------------+
                |                         |
                v                         v
        +---------------+         +---------------+
        | PostgreSQL    |         | AI Service    |
        | + PostGIS     |         | FastAPI       |
        +---------------+         +---------------+
                                         |
                                         v
                                  Computer Vision
                                         |
                                         v
                                  AI Analysis

                             |
                             v
                    +------------------+
                    | Admin Dashboard  |
                    +------------------+

## Backend Layers

### Controller
Responsible for HTTP requests, validation, and responses.

### Service
Responsible for business logic, workflows, orchestration, and calculations.

### Repository
Responsible for database access.

### Entity
Responsible for database mapping.

### DTO
Responsible for API request and response contracts.

### Mapper
Responsible for DTO <-> Entity conversion.

## Spatial Data

PostGIS stores geographic locations.

Example:

POINT(78.4867 17.3850)

SRID:
4326

Longitude:
78.4867

Latitude:
17.3850

## Complaint Flow

1. Citizen submits complaint.
2. Backend validates request.
3. Backend creates complaint.
4. Latitude/longitude are converted to a PostGIS Point.
5. Complaint is saved.
6. Complaint ID is returned.
7. AI analysis is triggered.
8. AI returns severity/confidence.
9. Backend stores AI analysis.
10. Duplicate detection runs.
11. Complaint is clustered.
12. Priority is calculated.
13. Department is selected.
14. Complaint is assigned.
15. Department resolves issue.
16. Resolution is stored.
17. Dashboard reflects the result.

## Important Design Principle

Build the reliable civic-data pipeline first.

Do not implement advanced AI before the complaint CRUD/workflow and spatial persistence are stable.
