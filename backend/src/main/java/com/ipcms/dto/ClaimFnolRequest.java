package com.ipcms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimFnolRequest {

    @NotNull
    private Long policyId;

    @NotNull
    private LocalDate incidentDate;

    @NotBlank
    private String lossDescription;

    private String incidentLocation;

    private BigDecimal claimedAmount = BigDecimal.ZERO;

    private String witnessStatement;

    public ClaimFnolRequest() {}

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public String getLossDescription() { return lossDescription; }
    public void setLossDescription(String lossDescription) { this.lossDescription = lossDescription; }

    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }

    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }

    public String getWitnessStatement() { return witnessStatement; }
    public void setWitnessStatement(String witnessStatement) { this.witnessStatement = witnessStatement; }
}
