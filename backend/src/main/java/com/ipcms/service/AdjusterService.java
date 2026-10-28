package com.ipcms.service;

import com.ipcms.dto.AdjusterAssignRequest;
import com.ipcms.model.AdjusterAssignment;
import com.ipcms.model.Claim;
import com.ipcms.repository.AdjusterAssignmentRepository;
import com.ipcms.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdjusterService {

    private final AdjusterAssignmentRepository adjusterRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;

    public AdjusterService(AdjusterAssignmentRepository adjusterRepository, ClaimRepository claimRepository, AuditService auditService) {
        this.adjusterRepository = adjusterRepository;
        this.claimRepository = claimRepository;
        this.auditService = auditService;
    }

    /**
     * FR9: Matches and dispatches field valuation personnel based on specialty & structural load limits
     */
    @Transactional
    public AdjusterAssignment dispatchAdjuster(AdjusterAssignRequest request, String username) {
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + request.getClaimId()));

        AdjusterAssignment assignment = new AdjusterAssignment(
                claim,
                request.getAdjusterName(),
                request.getSpecialty(),
                request.getStructuralLimit()
        );

        AdjusterAssignment saved = adjusterRepository.save(assignment);

        auditService.log(
                "ADJUSTER",
                claim.getId(),
                "DISPATCH_ADJUSTER",
                username,
                "HANDLER",
                "Adjuster " + request.getAdjusterName() + " dispatched for specialty " + request.getSpecialty() +
                " with structural limit $" + request.getStructuralLimit()
        );

        return saved;
    }

    @Transactional
    public AdjusterAssignment submitValuationReport(Long assignmentId, BigDecimal valuationAmount, String notes, String signatureHash) {
        AdjusterAssignment assignment = adjusterRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        assignment.setValuationAmount(valuationAmount);
        assignment.setInspectionNotes(notes);
        assignment.setDigitalSignatureHash(signatureHash);
        assignment.setStatus("COMPLETED");

        AdjusterAssignment saved = adjusterRepository.save(assignment);

        auditService.log(
                "ADJUSTER",
                assignment.getClaim().getId(),
                "VALUATION_SUBMITTED",
                assignment.getAdjusterName(),
                "FIELD_ADJUSTER",
                "Field valuation submitted: $" + valuationAmount + " with digital signature verification."
        );

        return saved;
    }

    public List<AdjusterAssignment> getAllAssignments() {
        return adjusterRepository.findAll();
    }
}
