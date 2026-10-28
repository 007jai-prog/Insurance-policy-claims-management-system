package com.ipcms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Appendix B & C Constraint: VARCHAR(30) UNIQUE NOT NULL check pattern validation
    // Regex matches standard codes: POL-, AUTO-, HLTH-, PROP-, CYBR-
    @NotBlank
    @Pattern(
        regexp = "^(POL|AUTO|HLTH|PROP|CYBR)-[A-Za-z0-9]{4,20}$",
        message = "Invalid policy identification format indicator"
    )
    @Column(name = "policy_number", nullable = false, unique = true, length = 30)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policyholder_id", nullable = false)
    private User policyholder;

    @NotBlank
    @Column(name = "product_type", nullable = false, length = 50)
    private String productType; // Auto Comprehensive, Commercial Property, Health Elite, Cyber Shield

    // Appendix B Constraint: DECIMAL(15,2) NOT NULL check parameters value non-negative
    @NotNull
    @DecimalMin(value = "0.00", message = "Base premium must be non-negative")
    @Column(name = "base_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePremium = BigDecimal.ZERO;

    @Column(name = "active_reserve", precision = 15, scale = 2)
    private BigDecimal activeReserve = BigDecimal.ZERO;

    @Column(name = "coverage_limit", precision = 15, scale = 2)
    private BigDecimal coverageLimit = new BigDecimal("500000.00");

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, PENDING_REVIEW, REJECTED, EXPIRED, CANCELLED

    @Column(name = "region_risk_index")
    private Double regionRiskIndex = 1.15;

    @Column(name = "pro_rata_adjusted")
    private Boolean proRataAdjusted = false;

    @Column(name = "asset_identifier", length = 100)
    private String assetIdentifier; // e.g., VIN for auto, Building ID for property (used in FR8 duplicate check)

    @Column(name = "rejection_date")
    private LocalDate rejectionDate; // Used for FR2/Appendix F 90-day refusal lock

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Policy() {}

    public Policy(String policyNumber, User policyholder, String productType, BigDecimal basePremium, BigDecimal coverageLimit, LocalDate expiryDate, String status, String assetIdentifier) {
        this.policyNumber = policyNumber;
        this.policyholder = policyholder;
        this.productType = productType;
        this.basePremium = basePremium;
        this.coverageLimit = coverageLimit;
        this.expiryDate = expiryDate;
        this.status = status;
        this.assetIdentifier = assetIdentifier;
        this.activeReserve = BigDecimal.ZERO;
        this.proRataAdjusted = false;
        this.regionRiskIndex = 1.15;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public User getPolicyholder() { return policyholder; }
    public void setPolicyholder(User policyholder) { this.policyholder = policyholder; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public BigDecimal getBasePremium() { return basePremium; }
    public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

    public BigDecimal getActiveReserve() { return activeReserve; }
    public void setActiveReserve(BigDecimal activeReserve) { this.activeReserve = activeReserve; }

    public BigDecimal getCoverageLimit() { return coverageLimit; }
    public void setCoverageLimit(BigDecimal coverageLimit) { this.coverageLimit = coverageLimit; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getRegionRiskIndex() { return regionRiskIndex; }
    public void setRegionRiskIndex(Double regionRiskIndex) { this.regionRiskIndex = regionRiskIndex; }

    public Boolean getProRataAdjusted() { return proRataAdjusted; }
    public void setProRataAdjusted(Boolean proRataAdjusted) { this.proRataAdjusted = proRataAdjusted; }

    public String getAssetIdentifier() { return assetIdentifier; }
    public void setAssetIdentifier(String assetIdentifier) { this.assetIdentifier = assetIdentifier; }

    public LocalDate getRejectionDate() { return rejectionDate; }
    public void setRejectionDate(LocalDate rejectionDate) { this.rejectionDate = rejectionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
