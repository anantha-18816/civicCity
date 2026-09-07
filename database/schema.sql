-- ==========================================
-- CivicAI Database Schema
-- PostgreSQL + PostGIS
-- ==========================================

CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================
-- USERS
-- =========================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'CITIZEN',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (role IN ('CITIZEN', 'OFFICER', 'ADMIN'))
);


-- =========================
-- DEPARTMENTS
-- =========================

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    code VARCHAR(50) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =========================
-- COMPLAINT CLUSTERS
-- =========================

CREATE TABLE complaint_clusters (
    id BIGSERIAL PRIMARY KEY,

    issue_type VARCHAR(50) NOT NULL,

    location GEOMETRY(Point, 4326) NOT NULL,

    report_count INTEGER NOT NULL DEFAULT 0,

    severity VARCHAR(20),

    priority INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =========================
-- COMPLAINTS
-- =========================

CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    department_id BIGINT,

    cluster_id BIGINT,

    issue_type VARCHAR(50) NOT NULL,

    title VARCHAR(200) NOT NULL,

    description TEXT,

    image_url TEXT,

    location GEOMETRY(Point, 4326) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',

    priority INTEGER NOT NULL DEFAULT 4,

    duplicate_of_id BIGINT,

    duplicate_score DOUBLE PRECISION,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    resolved_at TIMESTAMPTZ,

    CONSTRAINT fk_complaint_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_complaint_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id),

    CONSTRAINT fk_complaint_cluster
        FOREIGN KEY (cluster_id)
        REFERENCES complaint_clusters(id),

    CONSTRAINT fk_complaint_duplicate
        FOREIGN KEY (duplicate_of_id)
        REFERENCES complaints(id),

    CONSTRAINT chk_complaint_status
        CHECK (
            status IN (
                'SUBMITTED',
                'AI_ANALYZED',
                'ASSIGNED',
                'IN_PROGRESS',
                'RESOLVED',
                'REJECTED'
            )
        ),

    CONSTRAINT chk_complaint_priority
        CHECK (priority BETWEEN 1 AND 4)
);


-- =========================
-- AI ANALYSIS
-- =========================

CREATE TABLE complaint_ai_analysis (
    id BIGSERIAL PRIMARY KEY,

    complaint_id BIGINT NOT NULL UNIQUE,

    detected_object VARCHAR(100),

    severity VARCHAR(20),

    confidence DOUBLE PRECISION,

    estimated_length_m DOUBLE PRECISION,

    estimated_width_m DOUBLE PRECISION,

    road_risk VARCHAR(20),

    model_version VARCHAR(100),

    image_hash BIGINT,

    analyzed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_complaint
        FOREIGN KEY (complaint_id)
        REFERENCES complaints(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_ai_confidence
        CHECK (
            confidence IS NULL
            OR confidence BETWEEN 0 AND 1
        )
);


-- =========================
-- RESOLUTIONS
-- =========================

CREATE TABLE resolutions (
    id BIGSERIAL PRIMARY KEY,

    complaint_id BIGINT NOT NULL UNIQUE,

    officer_id BIGINT,

    before_image_url TEXT,

    after_image_url TEXT,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    notes TEXT,

    resolved_at TIMESTAMPTZ,

    verified_at TIMESTAMPTZ,

    CONSTRAINT fk_resolution_complaint
        FOREIGN KEY (complaint_id)
        REFERENCES complaints(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resolution_officer
        FOREIGN KEY (officer_id)
        REFERENCES users(id),

    CONSTRAINT chk_verification_status
        CHECK (
            verification_status IN (
                'PENDING',
                'VERIFIED',
                'REJECTED'
            )
        )
);


-- =========================
-- GEOSPATIAL INDEXES
-- =========================

CREATE INDEX idx_complaints_location
ON complaints
USING GIST (location);


CREATE INDEX idx_clusters_location
ON complaint_clusters
USING GIST (location);


-- =========================
-- COMMON QUERY INDEXES
-- =========================

CREATE INDEX idx_complaints_status
ON complaints(status);


CREATE INDEX idx_complaints_issue_type
ON complaints(issue_type);


CREATE INDEX idx_complaints_created_at
ON complaints(created_at);


CREATE INDEX idx_complaints_department
ON complaints(department_id);


CREATE INDEX idx_complaints_cluster
ON complaints(cluster_id);