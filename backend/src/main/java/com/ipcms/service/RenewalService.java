package com.ipcms.service;

import com.ipcms.model.Claim;
import com.ipcms.model.Policy;
import com.ipcms.model.RenewalNotice;
import com.ipcms.repository.ClaimRepository;
import com.ipcms.repository.PolicyRepository;
import com.ipcms.repository.RenewalNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RenewalService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final RenewalNoticeRepository renewalNoticeRepository;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public RenewalService(PolicyRepository policyRepository, ClaimRepository claimRepository, RenewalNoticeRepository renewalNoticeRepository, AuditService auditService) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.renewalNoticeRepository = renewalNoticeRepository;
        this.auditService = auditService;
    }

    /**
     * FR5: Dispatches renewal notices for policies expiring within 45 days.
     * Incorporates claims frequency adjustment factor into renewal premium.
     */
    @Transactional
    public List<RenewalNotice> runRenewalPipeline() {
        LocalDate today = LocalDate.now();
        LocalDate targetWindow = today.plusDays(45);

        List<Policy> upcomingExpiring = policyRepository.findByExpiryDateBetween(today, targetWindow);
        List<RenewalNotice> generatedNotices = new ArrayList<>();

        for (Policy policy : upcomingExpiring) {
            // Check if already dispatched
            List<RenewalNotice> existing = renewalNoticeRepository.findByPolicyId(policy.getId());
            if (!existing.isEmpty()) continue;

            // Claims frequency factor check
            List<Claim> policyClaims = claimRepository.findByPolicyId(policy.getId());
            int claimsCount = policyClaims.size();
            double frequencyFactor = 1.0 + (claimsCount * 0.15); // +15% per claim

            BigDecimal adjustedPremium = policy.getBasePremium()
                    .multiply(BigDecimal.valueOf(frequencyFactor))
                    .setScale(2, RoundingMode.HALF_UP);

            String noticeNumber = "RNW-" + policy.getPolicyNumber().replace("POL-", "").replace("AUTO-", "") + "-" + (random.nextInt(900) + 100);

            RenewalNotice notice = new RenewalNotice(
                    policy,
                    noticeNumber,
                    today, // Programmatically dispatched exactly 45 days prior to expiration
                    policy.getExpiryDate(),
                    adjustedPremium,
                    frequencyFactor
            );

            RenewalNotice saved = renewalNoticeRepository.save(notice);
            generatedNotices.add(saved);

            auditService.log(
                    "RENEWAL",
                    policy.getId(),
                    "DISPATCH_RENEWAL",
                    "SYSTEM_PIPELINE",
                    "AUTOMATION",
                    "45-Day Renewal notice " + noticeNumber + " compiled with frequency factor " + frequencyFactor + ". Adjusted premium: $" + adjustedPremium
            );
        }

        return generatedNotices;
    }

    public List<RenewalNotice> getAllNotices() {
        return renewalNoticeRepository.findAll();
    }
}
