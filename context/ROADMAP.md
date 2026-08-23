# CivicAI Roadmap

## Phase 1 - Foundation

- [x] Git repository
- [x] Project structure
- [x] Spring Boot
- [x] PostgreSQL
- [x] PostGIS
- [x] Database schema
- [x] Health endpoint
- [x] AI service skeleton
- [x] Docker skeleton

## Phase 2 - Complaint Management

- [x] Complaint entity
- [x] Complaint request DTO
- [x] Complaint response DTO
- [x] Complaint repository
- [x] Complaint service
- [x] Complaint controller
- [x] Validation
- [x] Error handling
- [x] POST /api/complaints
- [x] GET /api/complaints/{id}
- [x] GET /api/complaints
- [x] Update complaint status
- [x] Verify database persistence
- [x] Verify PostGIS location

## Phase 3 - AI Analysis

- [x] AI service API
- [x] Image upload
- [x] Pothole detection
- [x] Severity estimation
- [x] Confidence score
- [x] AI analysis database persistence

## Phase 4 - Duplicate Detection

- [x] Nearby complaint comparison
- [x] Spatial distance calculation
- [x] Image similarity
- [x] Duplicate score
- [x] Duplicate complaint linking

## Phase 5 - Complaint Clustering

- [x] Geographic clustering
- [x] Issue-type clustering
- [x] Complaint cluster creation
- [x] Cluster severity
- [x] Cluster priority

## Phase 6 - Priority Engine

- [x] Implemented: weighted scoring across all six factors

Priority should consider:
- AI severity
- number of reports
- location
- road importance
- time unresolved
- safety risk

## Phase 7 - Department Routing

- [x] Department mapping
- [x] Automatic assignment
- [x] Assignment API
- [x] Department workload

## Phase 8 - Resolution

- [x] Status transitions
- [x] Resolution records
- [x] Before/after images
- [x] Resolution time
- [x] Citizen verification

## Phase 9 - Dashboard

- [ ] Complaint map
- [ ] Heatmap
- [ ] Priority complaints
- [ ] Department statistics
- [ ] Resolution statistics
- [ ] AI insights

## Phase 10 - Mobile

- [ ] Login
- [ ] Complaint creation
- [ ] Camera
- [ ] GPS
- [ ] Complaint tracking
- [ ] Notifications

## Phase 11 - Production

- [ ] Docker
- [ ] CI/CD
- [ ] Authentication
- [ ] Authorization
- [ ] Logging
- [ ] Monitoring
- [ ] API documentation
- [ ] Deployment





