package com.ipcms.dto;

import jakarta.validation.constraints.NotBlank;

public class FraudReviewRequest {

    @NotBlank
    private String action; // FREEZE_PAYMENT, RELEASE_PAYMENT, REFER_SIU

    private String reviewNotes;

    public FraudReviewRequest() {}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
