package com.ipcms.service;

import com.ipcms.dto.SubrogationRequest;
import com.ipcms.model.Claim;
import com.ipcms.model.SubrogationCase;
import com.ipcms.repository.ClaimRepository;
import com.ipcms.repository.SubrogationCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SubrogationService {

    private final SubrogationCaseRepository subrogationRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;

    public SubrogationService(SubrogationCaseRepository subrogationRepository, ClaimRepository claimRepository, AuditService auditService) {
        this.subrogationRepository = subrogationRepository;
        this.claimRepository = claimRepository;
        this.auditService = auditService;
    }

    /**
     * FR6: Subrogation case initiation with fault percentage and co-insurance partner share split
     */
    @Transactional
    public SubrogationCase createSubrogation(SubrogationRequest request, String username) {
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + request.getClaimId()));

        claim.setSubrogationEligible(true);
        claim.setThirdPartyFaultPercentage(request.getFaultPercentage());
        claimRepository.save(claim);

        SubrogationCase subrogationCase = new SubrogationCase(
                claim,
                request.getThirdPartyCarrier(),
                request.getFaultPercentage(),
                request.getTargetRecoveryAmount(),
                request.getCoInsurancePartnerShare()
        );

        SubrogationCase saved = subrogationRepository.save(subrogationCase);

        auditService.log(
                "SUBROGATION",
                claim.getId(),
                "CREATE_SUBROGATION",
                username,
                "HANDLER",
                "Subrogation initiated against " + request.getThirdPartyCarrier() + " with " + request.getFaultPercentage() +
                "% fault. Target recovery: $" + request.getTargetRecoveryAmount() + ". Co-insurance split: " + request.getCoInsurancePartnerShare() + "%"
        );

        return saved;
    }

    @Transactional
    public SubrogationCase recordRecovery(Long caseId, BigDecimal amountRecovered, String username) {
        SubrogationCase subCase = subrogationRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with ID: " + caseId));

        subCase.setRecoveredAmount(subCase.getRecoveredAmount().add(amountRecovered));
        if (subCase.getRecoveredAmount().compareTo(subCase.getTargetRecoveryAmount()) >= 0) {
            subCase.setStatus("RECOVERED");
        } else {
            subCase.setStatus("PARTIAL_RECOVERY");
        }

        SubrogationCase saved = subrogationRepository.save(subCase);

        auditService.log(
                "SUBROGATION",
                subCase.getId(),
                "RECOVER_FUNDS",
                username,
                "HANDLER",
                "Recorded recovery of $" + amountRecovered + " from " + subCase.getThirdPartyCarrier()
        );

        return saved;
    }

    public List<SubrogationCase> getAllCases() {
        return subrogationRepository.findAll();
    }
}
