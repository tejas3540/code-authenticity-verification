package com.vericode.vericode_backend.dto;

public class StartAssessmentRequestDTO {
    private boolean consentConfirmed;

    public boolean isConsentConfirmed() { return consentConfirmed; }
    public void setConsentConfirmed(boolean consentConfirmed) { this.consentConfirmed = consentConfirmed; }
}
