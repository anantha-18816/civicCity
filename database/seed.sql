-- ==========================================
-- CivicAI Seed Data
-- PostgreSQL + PostGIS
-- ==========================================

-- =========================
-- USERS
-- =========================

INSERT INTO users (name, email, password_hash, role)
VALUES
    -- password: CivicAI@123
    ('Citizen One',    'citizen1@example.com',    '$2a$10$e8IFU.DVgkZayW1CMZIZZuacchSQviIZCK472K5ws1qimyj2Mh5iu', 'CITIZEN'),
    -- password: CivicAI@123
    ('Citizen Two',    'citizen2@example.com',    '$2a$10$e8IFU.DVgkZayW1CMZIZZuacchSQviIZCK472K5ws1qimyj2Mh5iu', 'CITIZEN'),
    -- password: officer123
    ('Officer One',    'officer1@example.com',    '$2a$10$nBxBtX2hWFesB1e23p3gPu161EF5Y6upWfM/s.B1UaKZR/UCtyjZe', 'OFFICER'),
    -- password: CivicAI@123
    ('Admin One',      'admin1@example.com',      '$2a$10$e8IFU.DVgkZayW1CMZIZZuacchSQviIZCK472K5ws1qimyj2Mh5iu', 'ADMIN')
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