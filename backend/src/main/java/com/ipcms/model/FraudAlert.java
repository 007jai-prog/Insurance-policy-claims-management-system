package com.ipcms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "alert_category", nullable = false, length = 60)
    private String alertCategory; // DUPLICATE_ASSET_CLAIM, INFLATED_RESERVE_VELOCITY, RECENT_ONBOARDING_INCIDENT

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore; // 0-100

    @Column(name = "alert_description", nullable = false, length = 1000)
    private String alertDescription;

    @Column(name = "duplicate_reference_id", length = 100)
    private String duplicateReferenceId; // Matches asset ID or previous claim ID (FR8)

    @Column(name = "is_reviewed", nullable = false)
    private Boolean isReviewed = false;

    @Column(name = "reviewed_by", length = 60)
    private String reviewedBy;

    @Column(name = "review_action", length = 40)
    private String reviewAction; // PAYMENT_FROZEN, CLEARED, REFERRED_TO_SIU

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt = LocalDateTime.now();

    public FraudAlert() {}

    public FraudAlert(Claim claim, String alertCategory, Integer riskScore, String alertDescription, String duplicateReferenceId) {
        this.claim = claim;
        this.alertCategory = alertCategory;
        this.riskScore = riskScore;
        this.alertDescription = alertDescription;
        this.duplicateReferenceId = duplicateReferenceId;
        this.isReviewed = false;
        this.flaggedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public String getAlertCategory() { return alertCategory; }
    public void setAlertCategory(String alertCategory) { this.alertCategory = alertCategory; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getAlertDescription() { return alertDescription; }
    public void setAlertDescription(String alertDescription) { this.alertDescription = alertDescription; }

    public String getDuplicateReferenceId() { return duplicateReferenceId; }
    public void setDuplicateReferenceId(String duplicateReferenceId) { this.duplicateReferenceId = duplicateReferenceId; }

    public Boolean getIsReviewed() { return isReviewed; }
    public void setIsReviewed(Boolean isReviewed) { this.isReviewed = isReviewed; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewAction() { return reviewAction; }
    public void setReviewAction(String reviewAction) { this.reviewAction = reviewAction; }

    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }
}
