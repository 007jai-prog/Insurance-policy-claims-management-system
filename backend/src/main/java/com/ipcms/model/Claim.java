package com.ipcms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "claim_number", nullable = false, unique = true, length = 35)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @NotNull
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    // Appendix B Constraint: gross_reserve DECIMAL(15,2) REQUIRED default parameter set to 0.00
    @NotNull
    @DecimalMin(value = "0.00", message = "Gross reserve must be non-negative")
    @Column(name = "gross_reserve", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossReserve = BigDecimal.ZERO;

    @NotBlank
    @Column(name = "loss_description", nullable = false, length = 2000)
    private String lossDescription;

    @NotBlank
    @Column(nullable = false, length = 35)
    private String status = "FILED"; // FILED, UNDER_INVESTIGATION, APPROVED, SETTLED, REJECTED, FROZEN

    @Column(name = "incident_location", length = 200)
    private String incidentLocation;

    @Column(name = "witness_statement", length = 1000)
    private String witnessStatement;

    @Column(name = "claimed_amount", precision = 15, scale = 2)
    private BigDecimal claimedAmount = BigDecimal.ZERO;

    @Column(name = "approved_payout", precision = 15, scale = 2)
    private BigDecimal approvedPayout = BigDecimal.ZERO;

    // FR8: High-risk indicator and payment freeze
    @Column(name = "fraud_risk_score")
    private Integer fraudRiskScore = 15; // 0 - 100

    @Column(name = "is_payment_frozen", nullable = false)
    private Boolean isPaymentFrozen = false;

    // FR6: Subrogation flag
    @Column(name = "subrogation_eligible")
    private Boolean subrogationEligible = false;

    @Column(name = "third_party_fault_percentage")
    private Integer thirdPartyFaultPercentage = 0;

    // Appendix F: SLA deadline (14 calendar days from notification)
    @Column(name = "sla_deadline")
    private LocalDate slaDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Claim() {
        this.grossReserve = BigDecimal.ZERO;
        this.isPaymentFrozen = false;
        this.createdAt = LocalDateTime.now();
        this.slaDeadline = LocalDate.now().plusDays(14);
    }

    public Claim(String claimNumber, Policy policy, User reporter, LocalDate incidentDate, BigDecimal grossReserve, String lossDescription, String incidentLocation, BigDecimal claimedAmount) {
        this.claimNumber = claimNumber;
        this.policy = policy;
        this.reporter = reporter;
        this.incidentDate = incidentDate;
        this.grossReserve = grossReserve != null ? grossReserve : BigDecimal.ZERO;
        this.lossDescription = lossDescription;
        this.incidentLocation = incidentLocation;
        this.claimedAmount = claimedAmount;
        this.status = "FILED";
        this.fraudRiskScore = 15;
        this.isPaymentFrozen = false;
        this.subrogationEligible = false;
        this.thirdPartyFaultPercentage = 0;
        this.createdAt = LocalDateTime.now();
        this.slaDeadline = LocalDate.now().plusDays(14);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public BigDecimal getGrossReserve() { return grossReserve; }
    public void setGrossReserve(BigDecimal grossReserve) { this.grossReserve = grossReserve; }

    public String getLossDescription() { return lossDescription; }
    public void setLossDescription(String lossDescription) { this.lossDescription = lossDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }

    public String getWitnessStatement() { return witnessStatement; }
    public void setWitnessStatement(String witnessStatement) { this.witnessStatement = witnessStatement; }

    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }

    public BigDecimal getApprovedPayout() { return approvedPayout; }
    public void setApprovedPayout(BigDecimal approvedPayout) { this.approvedPayout = approvedPayout; }

    public Integer getFraudRiskScore() { return fraudRiskScore; }
    public void setFraudRiskScore(Integer fraudRiskScore) { this.fraudRiskScore = fraudRiskScore; }

    public Boolean getIsPaymentFrozen() { return isPaymentFrozen; }
    public void setIsPaymentFrozen(Boolean isPaymentFrozen) { this.isPaymentFrozen = isPaymentFrozen; }

    public Boolean getSubrogationEligible() { return subrogationEligible; }
    public void setSubrogationEligible(Boolean subrogationEligible) { this.subrogationEligible = subrogationEligible; }

    public Integer getThirdPartyFaultPercentage() { return thirdPartyFaultPercentage; }
    public void setThirdPartyFaultPercentage(Integer thirdPartyFaultPercentage) { this.thirdPartyFaultPercentage = thirdPartyFaultPercentage; }

    public LocalDate getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDate slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
