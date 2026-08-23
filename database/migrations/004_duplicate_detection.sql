-- ==========================================
-- Migration 004: Duplicate Detection
-- Adds duplicate linkage to complaints and a
-- perceptual image hash to AI analysis rows.
-- ==========================================

ALTER TABLE complaints
    ADD COLUMN duplicate_of_id BIGINT,
    ADD COLUMN duplicate_score DOUBLE PRECISION;

ALTER TABLE complaints
    ADD CONSTRAINT fk_complaint_duplicate
        FOREIGN KEY (duplicate_of_id)
        REFERENCES complaints(id);

CREATE INDEX idx_complaints_duplicate_of
    ON complaints(duplicate_of_id);

ALTER TABLE complaint_ai_analysis
    ADD COLUMN image_hash BIGINT;
