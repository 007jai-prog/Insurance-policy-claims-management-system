package com.ipcms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UnderwritingOverrideRequest {

    @NotNull
    private Long policyId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal overridePremium;

    @NotBlank
    private String documentedJustification;

    @NotBlank
    private String supervisorSignoffUsername;

    @NotBlank
    private String supervisorPasscode;

    public UnderwritingOverrideRequest() {}

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public BigDecimal getOverridePremium() { return overridePremium; }
    public void setOverridePremium(BigDecimal overridePremium) { this.overridePremium = overridePremium; }

    public String getDocumentedJustification() { return documentedJustification; }
    public void setDocumentedJustification(String documentedJustification) { this.documentedJustification = documentedJustification; }

    public String getSupervisorSignoffUsername() { return supervisorSignoffUsername; }
    public void setSupervisorSignoffUsername(String supervisorSignoffUsername) { this.supervisorSignoffUsername = supervisorSignoffUsername; }

    public String getSupervisorPasscode() { return supervisorPasscode; }
    public void setSupervisorPasscode(String supervisorPasscode) { this.supervisorPasscode = supervisorPasscode; }
}
