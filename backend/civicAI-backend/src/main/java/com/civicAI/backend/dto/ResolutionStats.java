package com.civicAI.backend.dto;

import java.util.Map;

public class ResolutionStats {

    private long totalResolved;
    private long verified;
    private long rejected;
    private long pendingVerification;
    private double avgResolutionHours;

    public long getTotalResolved() {
        return totalResolved;
    }

    public void setTotalResolved(long totalResolved) {
        this.totalResolved = totalResolved;
    }

    public long getVerified() {
        return verified;
    }

    public void setVerified(long verified) {
        this.verified = verified;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getPendingVerification() {
        return pendingVerification;
    }

    public void setPendingVerification(long pendingVerification) {
        this.pendingVerification = pendingVerification;
    }

    public double getAvgResolutionHours() {
        return avgResolutionHours;
    }

    public void setAvgResolutionHours(double avgResolutionHours) {
        this.avgResolutionHours = avgResolutionHours;
    }
}

