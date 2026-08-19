-- ==========================================
-- CivicAI Seed Data
-- PostgreSQL + PostGIS
-- ==========================================

-- =========================
-- USERS
-- =========================

INSERT INTO users (name, email, password_hash, role)
VALUES
    ('Citizen One',    'citizen1@example.com',    '$2a$10$placeholderhashplaceholder', 'CITIZEN'),
    ('Citizen Two',    'citizen2@example.com',    '$2a$10$placeholderhashplaceholder', 'CITIZEN'),
    ('Officer One',    'officer1@example.com',    '$2a$10$placeholderhashplaceholder', 'OFFICER'),
    ('Admin One',      'admin1@example.com',      '$2a$10$placeholderhashplaceholder', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- =========================
-- DEPARTMENTS
-- =========================

INSERT INTO departments (name, code)
VALUES
    ('Roads and Infrastructure', 'ROADS'),
    ('Sanitation and Waste',     'SANITATION'),
    ('Public Lighting',          'LIGHTING'),
    ('Drainage and Water',       'DRAINAGE')
ON CONFLICT (code) DO NOTHING;