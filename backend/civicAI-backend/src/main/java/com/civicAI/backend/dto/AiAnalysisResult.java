package com.civicAI.backend.dto;

public class AiAnalysisResult {

    private String detectedObject;
    private String severity;
    private Double confidence;
    private Double estimatedLengthM;
    private Double estimatedWidthM;
    private String roadRisk;
    private String modelVersion;

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
}