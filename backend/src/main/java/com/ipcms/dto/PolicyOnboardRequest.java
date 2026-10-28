package com.ipcms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PolicyOnboardRequest {

    private Long policyholderId;

    @NotBlank
    private String productType; // Auto Comprehensive, Commercial Property, Health Elite, Cyber Shield

    @NotBlank
    private String assetIdentifier; // VIN, Building Parcel, etc.

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal assetValue;

    private Double regionRiskIndex = 1.15;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal coverageLimit = new BigDecimal("500000.00");

    private Integer applicantAge = 35;
    private Integer pastClaimsCount = 0;

    public PolicyOnboardRequest() {}

    public Long getPolicyholderId() { return policyholderId; }
    public void setPolicyholderId(Long policyholderId) { this.policyholderId = policyholderId; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getAssetIdentifier() { return assetIdentifier; }
    public void setAssetIdentifier(String assetIdentifier) { this.assetIdentifier = assetIdentifier; }

    public BigDecimal getAssetValue() { return assetValue; }
    public void setAssetValue(BigDecimal assetValue) { this.assetValue = assetValue; }

    public Double getRegionRiskIndex() { return regionRiskIndex; }
    public void setRegionRiskIndex(Double regionRiskIndex) { this.regionRiskIndex = regionRiskIndex; }

    public BigDecimal getCoverageLimit() { return coverageLimit; }
    public void setCoverageLimit(BigDecimal coverageLimit) { this.coverageLimit = coverageLimit; }

    public Integer getApplicantAge() { return applicantAge; }
    public void setApplicantAge(Integer applicantAge) { this.applicantAge = applicantAge; }

    public Integer getPastClaimsCount() { return pastClaimsCount; }
    public void setPastClaimsCount(Integer pastClaimsCount) { this.pastClaimsCount = pastClaimsCount; }
}
