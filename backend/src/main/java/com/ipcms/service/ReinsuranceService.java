package com.ipcms.service;

import com.ipcms.model.Claim;
import com.ipcms.model.ReinsuranceTreaty;
import com.ipcms.repository.ClaimRepository;
import com.ipcms.repository.ReinsuranceTreatyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReinsuranceService {

    private final ReinsuranceTreatyRepository treatyRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;

    public ReinsuranceService(ReinsuranceTreatyRepository treatyRepository, ClaimRepository claimRepository, AuditService auditService) {
        this.treatyRepository = treatyRepository;
        this.claimRepository = claimRepository;
        this.auditService = auditService;
    }

    /**
     * FR10: Evaluates excess loss distribution files across global risk-sharing treaty networks.
     * Launches reinsurance claim actions when retention limits are exceeded.
     */
    @Transactional
    public String evaluateTreatyAllocation(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        BigDecimal lossAmount = claim.getGrossReserve();
        List<ReinsuranceTreaty> activeTreaties = treatyRepository.findByActiveYear(2026);

        for (ReinsuranceTreaty treaty : activeTreaties) {
            // Check if claim exceeds retention limit
            if (lossAmount.compareTo(treaty.getRetentionLimit()) > 0) {
                BigDecimal excessLoss = lossAmount.subtract(treaty.getRetentionLimit());
                BigDecimal newUtilized = treaty.getUtilizedAmount().add(excessLoss);

                if (newUtilized.compareTo(treaty.getCapacityLimit()) <= 0) {
                    treaty.setUtilizedAmount(newUtilized);
                    treatyRepository.save(treaty);

                    auditService.log(
                            "REINSURANCE",
                            claim.getId(),
                            "TREATY_ALLOCATION",
                            "SYSTEM",
                            "AUTOMATION",
                            "Treaty [" + treaty.getTreatyName() + "] allocated excess loss of $" + excessLoss +
                            ". Remaining capacity: $" + treaty.getRemainingCapacity()
                    );

                    return "Excess loss of $" + excessLoss + " successfully allocated to treaty: " + treaty.getTreatyName();
                }
            }
        }

        return "Loss within self-insured retention bounds or treaty capacity limit reached.";
    }

    public List<ReinsuranceTreaty> getAllTreaties() {
        return treatyRepository.findAll();
    }
}
