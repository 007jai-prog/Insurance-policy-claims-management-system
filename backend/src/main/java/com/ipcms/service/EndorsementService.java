package com.ipcms.service;

import com.ipcms.dto.EndorsementRequest;
import com.ipcms.model.Endorsement;
import com.ipcms.model.Policy;
import com.ipcms.repository.EndorsementRepository;
import com.ipcms.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EndorsementService {

    private final EndorsementRepository endorsementRepository;
    private final PolicyRepository policyRepository;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public EndorsementService(EndorsementRepository endorsementRepository, PolicyRepository policyRepository, AuditService auditService) {
        this.endorsementRepository = endorsementRepository;
        this.policyRepository = policyRepository;
        this.auditService = auditService;
    }

    /**
     * FR4: Mid-term modifications with pro-rata financial adjustments and historical archiving
     */
    @Transactional
    public Endorsement processEndorsement(Long policyId, EndorsementRequest request, String username) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        // Archive current configuration into JSON snapshot
        String historicalSnapshot = String.format(
                "{\"policyNumber\":\"%s\",\"productType\":\"%s\",\"basePremium\":\"%s\",\"coverageLimit\":\"%s\",\"expiryDate\":\"%s\"}",
                policy.getPolicyNumber(),
                policy.getProductType(),
                policy.getBasePremium(),
                policy.getCoverageLimit(),
                policy.getExpiryDate()
        );

        // Calculate Pro-Rata adjustment for remaining term days
        long totalDaysInTerm = 365;
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), policy.getExpiryDate());
        if (daysRemaining < 0) daysRemaining = 0;
        double proRataRatio = (double) daysRemaining / totalDaysInTerm;

        BigDecimal coverageDelta = request.getCoverageLimitDelta() != null ? request.getCoverageLimitDelta() : new BigDecimal("50000.00");
        BigDecimal fullTermDelta = coverageDelta.multiply(new BigDecimal("0.02"));
        BigDecimal proRataAdjustment = fullTermDelta.multiply(BigDecimal.valueOf(proRataRatio)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal newBasePremium = policy.getBasePremium().add(proRataAdjustment);
        policy.setBasePremium(newBasePremium);
        policy.setCoverageLimit(policy.getCoverageLimit().add(coverageDelta));
        policy.setProRataAdjusted(true);
        policyRepository.save(policy);

        String endorsementNumber = "END-" + policy.getPolicyNumber().replace("POL-", "").replace("AUTO-", "").replace("HLTH-", "").replace("PROP-", "").replace("CYBR-", "") + "-" + (random.nextInt(900) + 100);

        Endorsement endorsement = new Endorsement(
                policy,
                endorsementNumber,
                request.getAlterationType(),
                proRataAdjustment,
                newBasePremium,
                historicalSnapshot,
                request.getJustificationNotes()
        );

        Endorsement saved = endorsementRepository.save(endorsement);

        auditService.log(
                "POLICY",
                policy.getId(),
                "ENDORSEMENT",
                username,
                "OPERATOR",
                "Mid-term endorsement " + endorsementNumber + " applied. Pro-rata premium adjustment: $" + proRataAdjustment + ". Archived prior state."
        );

        return saved;
    }

    public List<Endorsement> getPolicyEndorsements(Long policyId) {
        return endorsementRepository.findByPolicyIdOrderByProcessedAtDesc(policyId);
    }
}
