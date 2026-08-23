package com.civicAI.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificationRequest {

    @NotBlank(message = "verificationStatus is required")
    private String verificationStatus;

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
