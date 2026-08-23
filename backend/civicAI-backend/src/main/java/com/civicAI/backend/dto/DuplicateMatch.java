package com.civicAI.backend.dto;

public class DuplicateMatch {

    private boolean duplicate;
    private Long duplicateOfId;
    private double score;
    private Double distanceM;
    private Double imageSimilarity;

    public static DuplicateMatch none() {
        return new DuplicateMatch();
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Long getDuplicateOfId() {
        return duplicateOfId;
    }

    public void setDuplicateOfId(Long duplicateOfId) {
        this.duplicateOfId = duplicateOfId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Double getDistanceM() {
        return distanceM;
    }

    public void setDistanceM(Double distanceM) {
        this.distanceM = distanceM;
    }

    public Double getImageSimilarity() {
        return imageSimilarity;
    }

    public void setImageSimilarity(Double imageSimilarity) {
        this.imageSimilarity = imageSimilarity;
    }
}
