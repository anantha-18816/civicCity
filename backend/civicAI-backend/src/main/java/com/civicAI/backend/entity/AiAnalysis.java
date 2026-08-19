package com.civicAI.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "complaint_ai_analysis")
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false, unique = true)
    private Long complaintId;

    @Column(name = "detected_object", length = 100)
    private String detectedObject;

    @Column(length = 20)
    private String severity;

    private Double confidence;

    @Column(name = "estimated_length_m")
    private Double estimatedLengthM;

    @Column(name = "estimated_width_m")
    private Double estimatedWidthM;

    @Column(name = "road_risk", length = 20)
    private String roadRisk;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "analyzed_at", nullable = false)
    private OffsetDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        if (analyzedAt == null) {
            analyzedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getDetectedObject() {
        return detectedObject;
    }

    public void setDetectedObject(String detectedObject) {
        this.detectedObject = detectedObject;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Double getEstimatedLengthM() {
        return estimatedLengthM;
    }

    public void setEstimatedLengthM(Double estimatedLengthM) {
        this.estimatedLengthM = estimatedLengthM;
    }

    public Double getEstimatedWidthM() {
        return estimatedWidthM;
    }

    public void setEstimatedWidthM(Double estimatedWidthM) {
        this.estimatedWidthM = estimatedWidthM;
    }

    public String getRoadRisk() {
        return roadRisk;
    }

    public void setRoadRisk(String roadRisk) {
        this.roadRisk = roadRisk;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}