# CivicAI - OpenCode Development Instructions

## Project

CivicAI is an AI-powered civic infrastructure intelligence platform.

The first major use case is pothole/road-damage reporting. Citizens submit issue details, images, and GPS coordinates; the platform stores, analyzes, clusters, prioritizes, routes, and tracks civic complaints.

## Development Rules

1. Inspect the existing repository before changing code.
2. Treat repository code as the implementation source of truth.
3. Treat PROJECT_CONTEXT.md, ROADMAP.md, and ARCHITECTURE.md as project intent/context.
4. Preserve working functionality.
5. Do not rewrite working code unnecessarily.
6. Run compilation/tests after meaningful changes.
7. Explain what was found and what files will be changed before major modifications.
8. Keep business logic out of controllers and repositories.

## Technology

Backend:
- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- Hibernate Spatial
- PostgreSQL
- PostGIS
- Maven Wrapper

AI:
- Python
- FastAPI
- Computer Vision / ML

Database:
- PostgreSQL 18.4
- PostGIS 3.6.2

Frontend:
- Flutter mobile app
- Admin dashboard

Infrastructure:
- Docker / Docker Compose

## Current Backend Structure

backend/civicAI-backend/src/main/java/com/civicAI/backend/

- BackendApplication.java
- config/
- controller/
- dto/
- entity/
- exception/
- mapper/
- repository/
- service/
- util/

## Working Endpoint

GET /api/health

Expected response:

{
  "status": "UP",
  "version": "0.0.1",
  "service": "CivicAI Backend"
}

## Database

Database: civicai_db
Host: localhost
Port: 5432

PostGIS is installed and working.

Database tables:
- users
- departments
- complaints
- complaint_ai_analysis
- complaint_clusters
- resolutions
- spatial_ref_sys

The complaints location column uses geometry(Point,4326).

Schema:
database/schema.sql

Seed data:
database/seed.sql

## Current Development Stage

Foundation is complete. The immediate feature is Complaint Management.

Target flow:

Flutter
  -> ComplaintController
  -> ComplaintService
  -> ComplaintRepository
  -> PostgreSQL/PostGIS

The immediate milestone is a reliable POST /api/complaints endpoint.

Example request:

{
  "userId": 1,
  "issueType": "POTHOLE",
  "title": "Large pothole near college",
  "description": "Deep pothole causing danger to vehicles",
  "latitude": 17.3850,
  "longitude": 78.4867
}

The endpoint should validate input, create the Complaint entity, convert latitude/longitude into a PostGIS Point with SRID 4326, save it, and return a useful response DTO.

Do not jump into advanced AI until complaint persistence is reliable.

## Development Order

1. Complaint entity
2. Complaint request DTO
3. Complaint response DTO
4. Complaint repository
5. Complaint service
6. Complaint controller
7. Validation
8. Exception handling
9. POST /api/complaints
10. Verify database persistence
11. Verify PostGIS coordinates
12. Complaint retrieval APIs
13. Status management
14. AI-service integration
15. Duplicate detection
16. Clustering
17. Priority engine
18. Department assignment
19. Resolution workflow
20. Dashboard APIs
21. Flutter integration
22. Docker/deployment

## Coding Style

Prefer:
- constructor injection
- DTOs for API contracts
- validation annotations
- small services
- RESTful endpoints
- meaningful exceptions
- clear names

Avoid:
- business logic in controllers
- business logic in repositories
- exposing entities directly from APIs
- unnecessary abstractions
- architectural changes without justification
