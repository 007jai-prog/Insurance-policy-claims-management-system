package com.ipcms.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class EndorsementRequest {

    @NotBlank
    private String alterationType; // COVERAGE_INCREASE, DEDUCTIBLE_ALTERATION, ADD_RIDER, ASSET_UPDATE

    private BigDecimal coverageLimitDelta = BigDecimal.ZERO;

    private BigDecimal proRataPremiumAdjustment = BigDecimal.ZERO;

    @NotBlank
    private String justificationNotes;

    public EndorsementRequest() {}

    public String getAlterationType() { return alterationType; }
    public void setAlterationType(String alterationType) { this.alterationType = alterationType; }

    public BigDecimal getCoverageLimitDelta() { return coverageLimitDelta; }
    public void setCoverageLimitDelta(BigDecimal coverageLimitDelta) { this.coverageLimitDelta = coverageLimitDelta; }

    public BigDecimal getProRataPremiumAdjustment() { return proRataPremiumAdjustment; }
    public void setProRataPremiumAdjustment(BigDecimal proRataPremiumAdjustment) { this.proRataPremiumAdjustment = proRataPremiumAdjustment; }

    public String getJustificationNotes() { return justificationNotes; }
    public void setJustificationNotes(String justificationNotes) { this.justificationNotes = justificationNotes; }
}
