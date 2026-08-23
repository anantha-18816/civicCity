package com.civicAI.backend.dto;

public class ResolutionRequest {

    private Long officerId;
    private String beforeImageUrl;
    private String afterImageUrl;
    private String notes;

    public Long getOfficerId() {
        return officerId;
    }

    public void setOfficerId(Long officerId) {
        this.officerId = officerId;
    }

    public String getBeforeImageUrl() {
        return beforeImageUrl;
    }

    public void setBeforeImageUrl(String beforeImageUrl) {
        this.beforeImageUrl = beforeImageUrl;
    }

    public String getAfterImageUrl() {
        return afterImageUrl;
    }

    public void setAfterImageUrl(String afterImageUrl) {
        this.afterImageUrl = afterImageUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
