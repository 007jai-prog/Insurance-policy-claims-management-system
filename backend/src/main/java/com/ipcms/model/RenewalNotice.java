package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "renewal_notices")
public class RenewalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "notice_number", nullable = false, unique = true, length = 40)
    private String noticeNumber;

    @Column(name = "dispatch_date", nullable = false)
    private LocalDate dispatchDate; // Exactly 45 days prior to expiration (FR5)

    @Column(name = "current_expiry_date", nullable = false)
    private LocalDate currentExpiryDate;

    @Column(name = "adjusted_renewal_premium", precision = 15, scale = 2)
    private BigDecimal adjustedRenewalPremium;

    @Column(name = "claims_frequency_factor")
    private Double claimsFrequencyFactor = 1.0;

    @Column(name = "status", length = 30)
    private String status = "PENDING_ACCEPTANCE"; // PENDING_ACCEPTANCE, ACCEPTED, DECLINED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RenewalNotice() {}

    public RenewalNotice(Policy policy, String noticeNumber, LocalDate dispatchDate, LocalDate currentExpiryDate, BigDecimal adjustedRenewalPremium, Double claimsFrequencyFactor) {
        this.policy = policy;
        this.noticeNumber = noticeNumber;
        this.dispatchDate = dispatchDate;
        this.currentExpiryDate = currentExpiryDate;
        this.adjustedRenewalPremium = adjustedRenewalPremium;
        this.claimsFrequencyFactor = claimsFrequencyFactor;
        this.status = "PENDING_ACCEPTANCE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public String getNoticeNumber() { return noticeNumber; }
    public void setNoticeNumber(String noticeNumber) { this.noticeNumber = noticeNumber; }

    public LocalDate getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(LocalDate dispatchDate) { this.dispatchDate = dispatchDate; }

    public LocalDate getCurrentExpiryDate() { return currentExpiryDate; }
    public void setCurrentExpiryDate(LocalDate currentExpiryDate) { this.currentExpiryDate = currentExpiryDate; }

    public BigDecimal getAdjustedRenewalPremium() { return adjustedRenewalPremium; }
    public void setAdjustedRenewalPremium(BigDecimal adjustedRenewalPremium) { this.adjustedRenewalPremium = adjustedRenewalPremium; }

    public Double getClaimsFrequencyFactor() { return claimsFrequencyFactor; }
    public void setClaimsFrequencyFactor(Double claimsFrequencyFactor) { this.claimsFrequencyFactor = claimsFrequencyFactor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
