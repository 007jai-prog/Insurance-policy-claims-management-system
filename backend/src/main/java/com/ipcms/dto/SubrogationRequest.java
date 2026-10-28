package com.ipcms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SubrogationRequest {

    @NotNull
    private Long claimId;

    @NotBlank
    private String thirdPartyCarrier;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer faultPercentage;

    @NotNull
    private BigDecimal targetRecoveryAmount;

    private BigDecimal coInsurancePartnerShare = BigDecimal.ZERO;

    public SubrogationRequest() {}

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getThirdPartyCarrier() { return thirdPartyCarrier; }
    public void setThirdPartyCarrier(String thirdPartyCarrier) { this.thirdPartyCarrier = thirdPartyCarrier; }

    public Integer getFaultPercentage() { return faultPercentage; }
    public void setFaultPercentage(Integer faultPercentage) { this.faultPercentage = faultPercentage; }

    public BigDecimal getTargetRecoveryAmount() { return targetRecoveryAmount; }
    public void setTargetRecoveryAmount(BigDecimal targetRecoveryAmount) { this.targetRecoveryAmount = targetRecoveryAmount; }

    public BigDecimal getCoInsurancePartnerShare() { return coInsurancePartnerShare; }
    public void setCoInsurancePartnerShare(BigDecimal coInsurancePartnerShare) { this.coInsurancePartnerShare = coInsurancePartnerShare; }
}
