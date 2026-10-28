package com.ipcms.service;

import com.ipcms.dto.FraudReviewRequest;
import com.ipcms.model.Claim;
import com.ipcms.model.FraudAlert;
import com.ipcms.repository.ClaimRepository;
import com.ipcms.repository.FraudAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FraudService {

    private final FraudAlertRepository fraudAlertRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;

    public FraudService(FraudAlertRepository fraudAlertRepository, ClaimRepository claimRepository, AuditService auditService) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.claimRepository = claimRepository;
        this.auditService = auditService;
    }

    /**
     * FR8: Evaluates incoming claim behavioral risk and freezes payment if high risk.
     */
    @Transactional
    public FraudAlert triggerRiskAssessment(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        int score = 20;
        String reason = "Routine baseline underwriting profile.";

        if (claim.getClaimedAmount().doubleValue() > 25000) {
            score += 35;
            reason = "High loss valuation request exceeding standard retail thresholds.";
        }

        if (score >= 75) {
            claim.setFraudRiskScore(score);
            claim.setIsPaymentFrozen(true);
            claim.setStatus("FROZEN");
            claimRepository.save(claim);

            FraudAlert alert = new FraudAlert(
                    claim,
                    "INFLATED_RESERVE_VELOCITY",
                    score,
                    reason + " Automated payment freeze initiated pending security analyst review.",
                    claim.getClaimNumber()
            );

            FraudAlert saved = fraudAlertRepository.save(alert);
            auditService.log(
                    "FRAUD",
                    claim.getId(),
                    "FRAUD_FREEZE",
                    "ANALYTICS_ENGINE",
                    "SYSTEM",
                    "Claim payment frozen due to high anomaly risk score (" + score + "). Reason: " + reason
            );
            return saved;
        }

        return null;
    }

    /**
     * Formal Security Analyst Evaluation (Freeze, Release, or Refer)
     */
    @Transactional
    public FraudAlert reviewAlert(Long alertId, FraudReviewRequest request, String analystUsername) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));

        alert.setIsReviewed(true);
        alert.setReviewedBy(analystUsername);
        alert.setReviewAction(request.getAction());

        Claim claim = alert.getClaim();
        if ("RELEASE_PAYMENT".equalsIgnoreCase(request.getAction())) {
            claim.setIsPaymentFrozen(false);
            claim.setStatus("UNDER_INVESTIGATION");
            claimRepository.save(claim);
        } else if ("FREEZE_PAYMENT".equalsIgnoreCase(request.getAction())) {
            claim.setIsPaymentFrozen(true);
            claim.setStatus("FROZEN");
            claimRepository.save(claim);
        }

        FraudAlert saved = fraudAlertRepository.save(alert);

        auditService.log(
                "FRAUD",
                claim.getId(),
                "ANALYST_REVIEW",
                analystUsername,
                "ANALYST",
                "Fraud alert " + alertId + " reviewed with action: " + request.getAction() +
                ". Notes: " + request.getReviewNotes()
        );

        return saved;
    }

    public List<FraudAlert> getAllAlerts() {
        return fraudAlertRepository.findAll();
    }
}
