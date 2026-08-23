package com.civicAI.backend.dto;

import java.util.Map;

public class AiInsights {

    private Map<String, Long> detectionsByObject;
    private Map<String, Long> complaintsBySeverity;
    private double avgConfidence;
    private long duplicatesLinked;
    private long totalAnalyses;
    private String latestModelVersion;

    public Map<String, Long> getDetectionsByObject() {
        return detectionsByObject;
    }

    public void setDetectionsByObject(Map<String, Long> detectionsByObject) {
        this.detectionsByObject = detectionsByObject;
    }

    public Map<String, Long> getComplaintsBySeverity() {
        return complaintsBySeverity;
    }

    public void setComplaintsBySeverity(Map<String, Long> complaintsBySeverity) {
        this.complaintsBySeverity = complaintsBySeverity;
    }

    public double getAvgConfidence() {
        return avgConfidence;
    }

    public void setAvgConfidence(double avgConfidence) {
        this.avgConfidence = avgConfidence;
    }

    public long getDuplicatesLinked() {
        return duplicatesLinked;
    }

    public void setDuplicatesLinked(long duplicatesLinked) {
        this.duplicatesLinked = duplicatesLinked;
    }

    public long getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(long totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public String getLatestModelVersion() {
        return latestModelVersion;
    }

    public void setLatestModelVersion(String latestModelVersion) {
        this.latestModelVersion = latestModelVersion;
    }
}

