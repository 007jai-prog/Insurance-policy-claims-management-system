package com.ipcms.service;

import com.ipcms.dto.ClaimFnolRequest;
import com.ipcms.dto.ReserveUpdateRequest;
import com.ipcms.model.*;
import com.ipcms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ClaimsService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public ClaimsService(
            ClaimRepository claimRepository,
            PolicyRepository policyRepository,
            UserRepository userRepository,
            FraudAlertRepository fraudAlertRepository,
            AuditService auditService) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.auditService = auditService;
    }

    /**
     * Appendix H: POST /api/claims/fnol
     * FR3: Claims Registration and First Notice of Loss Processing
     * FR8: Duplicate asset check across distinct policy categories
     * Appendix F: 14-day SLA deadline setup
     */
    @Transactional
    public Claim registerFnol(ClaimFnolRequest request, String reporterUsername) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + request.getPolicyId()));

        User reporter = userRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found: " + reporterUsername));

        // Generate claim identifier: CLM-2026-XXXXX
        String claimNumber = "CLM-2026-" + String.format("%05d", random.nextInt(90000) + 10000);

        // Calculate initial reserve: 65% of claimed amount (FR3 reserve validation)
        BigDecimal claimed = request.getClaimedAmount() != null ? request.getClaimedAmount() : BigDecimal.ZERO;
        BigDecimal initialReserve = claimed.multiply(new BigDecimal("0.65")).setScale(2, RoundingMode.HALF_UP);
        if (initialReserve.compareTo(BigDecimal.ZERO) == 0) {
            initialReserve = new BigDecimal("1500.00");
        }

        Claim claim = new Claim(
                claimNumber,
                policy,
                reporter,
                request.getIncidentDate(),
                initialReserve,
                request.getLossDescription(),
                request.getIncidentLocation(),
                claimed
        );
        claim.setWitnessStatement(request.getWitnessStatement());
        claim.setSlaDeadline(LocalDate.now().plusDays(14)); // Appendix F: 14 calendar days

        // FR8 Anomaly Check: Duplicate asset claims across distinct policies
        String assetId = policy.getAssetIdentifier();
        if (assetId != null && !assetId.isBlank()) {
            List<Claim> existingClaimsForAsset = claimRepository.findByPolicy_AssetIdentifier(assetId);
            if (!existingClaimsForAsset.isEmpty()) {
                claim.setFraudRiskScore(88);
                claim.setIsPaymentFrozen(true);
                claim.setStatus("FROZEN");

                FraudAlert alert = new FraudAlert(
                        claim,
                        "DUPLICATE_ASSET_CLAIM",
                        88,
                        "Anomalous Analytics: Multiple claims filed for identical asset identifier '" + assetId + "' across policy records. Payment dispatch automatically frozen.",
                        assetId
                );
                fraudAlertRepository.save(alert);
            }
        }

        Claim saved = claimRepository.save(claim);

        // Update Policy active reserve
        BigDecimal currentActiveReserve = policy.getActiveReserve() != null ? policy.getActiveReserve() : BigDecimal.ZERO;
        policy.setActiveReserve(currentActiveReserve.add(initialReserve));
        policyRepository.save(policy);

        auditService.log(
                "CLAIM",
                saved.getId(),
                "CREATE",
                reporterUsername,
                reporter.getRole().name(),
                "FNOL Claim registered: " + claimNumber + " on Policy " + policy.getPolicyNumber() + " with Initial Reserve: $" + initialReserve
        );

        return saved;
    }

    /**
     * Appendix H: PUT /api/claims/reserve/{id}
     * Appendix A: Modify Reserve Totals (Claims Handler, Admin)
     */
    @Transactional
    public Claim updateReserve(Long claimId, ReserveUpdateRequest request, String modifierUsername, String role) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        BigDecimal oldReserve = claim.getGrossReserve();
        claim.setGrossReserve(request.getNewReserve());

        // Update policy total active reserve delta
        Policy policy = claim.getPolicy();
        BigDecimal diff = request.getNewReserve().subtract(oldReserve);
        BigDecimal currentActiveReserve = policy.getActiveReserve() != null ? policy.getActiveReserve() : BigDecimal.ZERO;
        policy.setActiveReserve(currentActiveReserve.add(diff));
        policyRepository.save(policy);

        Claim updatedClaim = claimRepository.save(claim);

        auditService.log(
                "RESERVE",
                claim.getId(),
                "UPDATE_RESERVE",
                modifierUsername,
                role,
                "Manual adjustment of Gross Reserve from $" + oldReserve + " to $" + request.getNewReserve() +
                ". Justification: " + (request.getJustificationNotes() != null ? request.getJustificationNotes() : "Standard manual adjudication adjustment")
        );

        return updatedClaim;
    }

    @Transactional
    public Claim updateStatus(Long claimId, String newStatus, String username, String role) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        String oldStatus = claim.getStatus();
        claim.setStatus(newStatus);
        Claim updated = claimRepository.save(claim);

        auditService.log(
                "CLAIM",
                claim.getId(),
                "STATUS_CHANGE",
                username,
                role,
                "Claim status transitioned from " + oldStatus + " to " + newStatus
        );

        return updated;
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> getClaimsByReporter(Long reporterId) {
        return claimRepository.findByReporterId(reporterId);
    }
}
