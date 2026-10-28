package com.ipcms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdjusterAssignRequest {

    @NotNull
    private Long claimId;

    @NotBlank
    private String adjusterName;

    @NotBlank
    private String specialty; // AUTOMOTIVE_STRUCTURAL, COMMERCIAL_PROPERTY, CASUALTY

    private BigDecimal structuralLimit = new BigDecimal("100000.00");

    public AdjusterAssignRequest() {}

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getAdjusterName() { return adjusterName; }
    public void setAdjusterName(String adjusterName) { this.adjusterName = adjusterName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public BigDecimal getStructuralLimit() { return structuralLimit; }
    public void setStructuralLimit(BigDecimal structuralLimit) { this.structuralLimit = structuralLimit; }
}
