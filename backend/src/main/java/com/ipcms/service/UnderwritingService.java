package com.ipcms.service;

import com.ipcms.dto.PolicyOnboardRequest;
import com.ipcms.dto.UnderwritingOverrideRequest;
import com.ipcms.model.Policy;
import com.ipcms.model.Role;
import com.ipcms.model.User;
import com.ipcms.repository.PolicyRepository;
import com.ipcms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class UnderwritingService {

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public UnderwritingService(PolicyRepository policyRepository, UserRepository userRepository, AuditService auditService) {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * FR2: Dynamic Underwriting Engine and Rating Matrix calculation
     */
    public BigDecimal calculatePremium(String productType, BigDecimal assetValue, Double regionRiskIndex, Integer applicantAge, Integer pastClaims) {
        if (assetValue == null || assetValue.compareTo(BigDecimal.ZERO) <= 0) {
            assetValue = new BigDecimal("50000.00");
        }
        if (regionRiskIndex == null || regionRiskIndex <= 0) {
            regionRiskIndex = 1.15;
        }

        double baseRateFactor;
        switch (productType.toUpperCase()) {
            case "AUTO COMPREHENSIVE":
            case "AUTO":
                baseRateFactor = 0.035;
                break;
            case "COMMERCIAL PROPERTY":
            case "PROPERTY":
                baseRateFactor = 0.025;
                break;
            case "CYBER SHIELD":
            case "CYBER":
                baseRateFactor = 0.045;
                break;
            case "HEALTH ELITE":
            case "HEALTH":
            default:
                baseRateFactor = 0.030;
                break;
        }

        // Age factor
        double ageFactor = 1.0;
        if (applicantAge != null) {
            if (applicantAge < 25) ageFactor = 1.35;
            else if (applicantAge > 65) ageFactor = 1.20;
        }

        // Past claims multiplier
        double claimsFactor = 1.0 + ((pastClaims != null ? pastClaims : 0) * 0.18);

        BigDecimal premium = assetValue
                .multiply(BigDecimal.valueOf(baseRateFactor))
                .multiply(BigDecimal.valueOf(regionRiskIndex))
                .multiply(BigDecimal.valueOf(ageFactor))
                .multiply(BigDecimal.valueOf(claimsFactor))
                .setScale(2, RoundingMode.HALF_UP);

        return premium;
    }

    /**
     * Appendix H: POST /api/policy/onboard
     * Appendix C: Validates standard prefix format
     * Appendix F: Enforces 90-day refusal cooldown on rejected profiles
     */
    @Transactional
    public Policy onboardPolicy(PolicyOnboardRequest request, String initiatorUsername) {
        User policyholder;
        if (request.getPolicyholderId() != null) {
            policyholder = userRepository.findById(request.getPolicyholderId())
                    .orElseThrow(() -> new IllegalArgumentException("Policyholder not found with ID: " + request.getPolicyholderId()));
        } else {
            policyholder = userRepository.findByUsername(initiatorUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Initiator user not found: " + initiatorUsername));
        }

        // Appendix F: Check 90-day refusal rule for identical asset/risk
        List<Policy> pastPolicies = policyRepository.findByAssetIdentifier(request.getAssetIdentifier());
        for (Policy past : pastPolicies) {
            if ("REJECTED".equalsIgnoreCase(past.getStatus()) && past.getRejectionDate() != null) {
                long daysSinceRejection = ChronoUnit.DAYS.between(past.getRejectionDate(), LocalDate.now());
                if (daysSinceRejection < 90) {
                    throw new IllegalStateException("Refusal Cooldown Active: Rejected applicant profile is restricted from rewriting identical risks for 90 days (Days remaining: " + (90 - daysSinceRejection) + ")");
                }
            }
        }

        // Generate standard policy number matching Appendix C format: POL-XXXX-NNNNN
        String prefix = "POL";
        if (request.getProductType().toUpperCase().contains("AUTO")) prefix = "AUTO";
        else if (request.getProductType().toUpperCase().contains("HEALTH")) prefix = "HLTH";
        else if (request.getProductType().toUpperCase().contains("PROP")) prefix = "PROP";
        else if (request.getProductType().toUpperCase().contains("CYBER")) prefix = "CYBR";

        String uniqueCode = String.format("%05d", random.nextInt(90000) + 10000);
        String policyNumber = prefix + "-" + uniqueCode;

        BigDecimal basePremium = calculatePremium(
                request.getProductType(),
                request.getAssetValue(),
                request.getRegionRiskIndex(),
                request.getApplicantAge(),
                request.getPastClaimsCount()
        );

        Policy policy = new Policy(
                policyNumber,
                policyholder,
                request.getProductType(),
                basePremium,
                request.getCoverageLimit(),
                LocalDate.now().plusYears(1),
                "ACTIVE",
                request.getAssetIdentifier()
        );
        policy.setRegionRiskIndex(request.getRegionRiskIndex() != null ? request.getRegionRiskIndex() : 1.15);

        Policy savedPolicy = policyRepository.save(policy);

        auditService.log(
                "POLICY",
                savedPolicy.getId(),
                "CREATE",
                initiatorUsername,
                policyholder.getRole().name(),
                "New policy onboarded: " + policyNumber + " [" + request.getProductType() + "] with premium $" + basePremium
        );

        return savedPolicy;
    }

    /**
     * FR2: Manual Underwriter Override requiring documented justification & supervisor credentials
     */
    @Transactional
    public Policy overrideUnderwrite(UnderwritingOverrideRequest request, String underwriterUsername) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + request.getPolicyId()));

        User supervisor = userRepository.findByUsername(request.getSupervisorSignoffUsername())
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found: " + request.getSupervisorSignoffUsername()));

        if (supervisor.getRole() != Role.ADMIN && supervisor.getRole() != Role.UNDERWRITER) {
            throw new SecurityException("Supervisor sign-off requires UNDERWRITER or ADMIN credentials");
        }

        BigDecimal oldPremium = policy.getBasePremium();
        policy.setBasePremium(request.getOverridePremium());
        Policy updated = policyRepository.save(policy);

        auditService.log(
                "UNDERWRITING",
                policy.getId(),
                "OVERRIDE_UNDERWRITE",
                underwriterUsername,
                "UNDERWRITER",
                "Manual premium override from $" + oldPremium + " to $" + request.getOverridePremium() +
                ". Justification: " + request.getDocumentedJustification() +
                " | Supervisor Sign-off: " + request.getSupervisorSignoffUsername()
        );

        return updated;
    }
}
