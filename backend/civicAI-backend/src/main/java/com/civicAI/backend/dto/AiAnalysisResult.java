package com.civicAI.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiAnalysisResult {

    @JsonProperty("detected_object")
    private String detectedObject;

    private String severity;

    private Double confidence;

    @JsonProperty("estimated_length_m")
    private Double estimatedLengthM;

    @JsonProperty("estimated_width_m")
    private Double estimatedWidthM;

    @JsonProperty("road_risk")
    private String roadRisk;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("image_hash")
    private Long imageHash;

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

    public Long getImageHash() {
        return imageHash;
    }

    public void setImageHash(Long imageHash) {
        this.imageHash = imageHash;
    }
}