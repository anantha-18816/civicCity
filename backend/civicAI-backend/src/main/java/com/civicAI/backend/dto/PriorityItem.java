package com.civicAI.backend.dto;

public class PriorityItem {

    private Long complaintId;
    private String title;
    private String issueType;
    private String status;
    private Integer priority;
    private double score;
    private String severity;
    private String roadRisk;
    private Integer reportCount;
    private long ageDays;
    private Double latitude;
    private Double longitude;

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRoadRisk() {
        return roadRisk;
    }

    public void setRoadRisk(String roadRisk) {
        this.roadRisk = roadRisk;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public long getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(long ageDays) {
        this.ageDays = ageDays;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
