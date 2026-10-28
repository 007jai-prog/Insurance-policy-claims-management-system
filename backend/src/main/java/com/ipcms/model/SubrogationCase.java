package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subrogation_cases")
public class SubrogationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "third_party_carrier", nullable = false, length = 100)
    private String thirdPartyCarrier;

    @Column(name = "fault_percentage", nullable = false)
    private Integer faultPercentage; // e.g. 80%

    @Column(name = "target_recovery_amount", precision = 15, scale = 2)
    private BigDecimal targetRecoveryAmount;

    @Column(name = "recovered_amount", precision = 15, scale = 2)
    private BigDecimal recoveredAmount = BigDecimal.ZERO;

    @Column(name = "co_insurance_partner_share", precision = 5, scale = 2)
    private BigDecimal coInsurancePartnerShare = BigDecimal.ZERO; // Partner split percentage (FR6)

    @Column(name = "status", length = 30)
    private String status = "INITIATED"; // INITIATED, DEMAND_ISSUED, IN_LITIGATION, RECOVERED, CLOSED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public SubrogationCase() {}

    public SubrogationCase(Claim claim, String thirdPartyCarrier, Integer faultPercentage, BigDecimal targetRecoveryAmount, BigDecimal coInsurancePartnerShare) {
        this.claim = claim;
        this.thirdPartyCarrier = thirdPartyCarrier;
        this.faultPercentage = faultPercentage;
        this.targetRecoveryAmount = targetRecoveryAmount;
        this.coInsurancePartnerShare = coInsurancePartnerShare;
        this.recoveredAmount = BigDecimal.ZERO;
        this.status = "INITIATED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public String getThirdPartyCarrier() { return thirdPartyCarrier; }
    public void setThirdPartyCarrier(String thirdPartyCarrier) { this.thirdPartyCarrier = thirdPartyCarrier; }

    public Integer getFaultPercentage() { return faultPercentage; }
    public void setFaultPercentage(Integer faultPercentage) { this.faultPercentage = faultPercentage; }

    public BigDecimal getTargetRecoveryAmount() { return targetRecoveryAmount; }
    public void setTargetRecoveryAmount(BigDecimal targetRecoveryAmount) { this.targetRecoveryAmount = targetRecoveryAmount; }

    public BigDecimal getRecoveredAmount() { return recoveredAmount; }
    public void setRecoveredAmount(BigDecimal recoveredAmount) { this.recoveredAmount = recoveredAmount; }

    public BigDecimal getCoInsurancePartnerShare() { return coInsurancePartnerShare; }
    public void setCoInsurancePartnerShare(BigDecimal coInsurancePartnerShare) { this.coInsurancePartnerShare = coInsurancePartnerShare; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
