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

- [ ] Complaint entity
- [ ] Complaint request DTO
- [ ] Complaint response DTO
- [ ] Complaint repository
- [ ] Complaint service
- [ ] Complaint controller
- [ ] Validation
- [ ] Error handling
- [ ] POST /api/complaints
- [ ] GET /api/complaints/{id}
- [ ] GET /api/complaints
- [ ] Update complaint status
- [ ] Verify database persistence
- [ ] Verify PostGIS location

## Phase 3 - AI Analysis

- [ ] AI service API
- [ ] Image upload
- [ ] Pothole detection
- [ ] Severity estimation
- [ ] Confidence score
- [ ] AI analysis database persistence

## Phase 4 - Duplicate Detection

- [ ] Nearby complaint comparison
- [ ] Spatial distance calculation
- [ ] Image similarity
- [ ] Duplicate score
- [ ] Duplicate complaint linking

## Phase 5 - Complaint Clustering

- [ ] Geographic clustering
- [ ] Issue-type clustering
- [ ] Complaint cluster creation
- [ ] Cluster severity
- [ ] Cluster priority

## Phase 6 - Priority Engine

Priority should consider:
- AI severity
- number of reports
- location
- road importance
- time unresolved
- safety risk

## Phase 7 - Department Routing

- [ ] Department mapping
- [ ] Automatic assignment
- [ ] Assignment API
- [ ] Department workload

## Phase 8 - Resolution

- [ ] Status transitions
- [ ] Resolution records
- [ ] Before/after images
- [ ] Resolution time
- [ ] Citizen verification

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
