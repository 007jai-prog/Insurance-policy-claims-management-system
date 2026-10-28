package com.ipcms.controller;

import com.ipcms.dto.EndorsementRequest;
import com.ipcms.dto.PolicyOnboardRequest;
import com.ipcms.model.Endorsement;
import com.ipcms.model.Policy;
import com.ipcms.model.RenewalNotice;
import com.ipcms.model.User;
import com.ipcms.repository.PolicyRepository;
import com.ipcms.repository.UserRepository;
import com.ipcms.service.DocumentService;
import com.ipcms.service.EndorsementService;
import com.ipcms.service.RenewalService;
import com.ipcms.service.UnderwritingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private final UnderwritingService underwritingService;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final EndorsementService endorsementService;
    private final RenewalService renewalService;
    private final DocumentService documentService;

    public PolicyController(
            UnderwritingService underwritingService,
            PolicyRepository policyRepository,
            UserRepository userRepository,
            EndorsementService endorsementService,
            RenewalService renewalService,
            DocumentService documentService) {
        this.underwritingService = underwritingService;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.endorsementService = endorsementService;
        this.renewalService = renewalService;
        this.documentService = documentService;
    }

    /**
     * Appendix H: POST /api/policy/onboard
     * Initializes novel profile parameter data
     */
    @PostMapping("/onboard")
    public ResponseEntity<?> onboardPolicy(@Valid @RequestBody PolicyOnboardRequest request, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "system";
            Policy created = underwritingService.onboardPolicy(request, username);
            return ResponseEntity.ok(created);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Appendix H: GET /api/policy/all
     * Pulls complete system records matching user scope
     */
    @GetMapping("/all")
    public ResponseEntity<List<Policy>> getAllPolicies(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(List.of());
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }

        // Scope matching user role: HOLDER sees their own, Staff see all
        if (user.getRole() == com.ipcms.model.Role.HOLDER) {
            return ResponseEntity.ok(policyRepository.findByPolicyholderId(user.getId()));
        }
        return ResponseEntity.ok(policyRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicyById(@PathVariable Long id) {
        return policyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * FR4: Mid-term endorsement modification with pro-rata adjustment
     */
    @PostMapping("/endorse/{id}")
    public ResponseEntity<?> processEndorsement(@PathVariable Long id, @Valid @RequestBody EndorsementRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        try {
            Endorsement endorsement = endorsementService.processEndorsement(id, request, username);
            return ResponseEntity.ok(endorsement);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/endorsements")
    public ResponseEntity<List<Endorsement>> getEndorsements(@PathVariable Long id) {
        return ResponseEntity.ok(endorsementService.getPolicyEndorsements(id));
    }

    /**
     * FR5: Automated renewal cycle compilation
     */
    @PostMapping("/renewals/run")
    public ResponseEntity<List<RenewalNotice>> triggerRenewals() {
        return ResponseEntity.ok(renewalService.runRenewalPipeline());
    }

    @GetMapping("/renewals/all")
    public ResponseEntity<List<RenewalNotice>> getAllRenewalNotices() {
        return ResponseEntity.ok(renewalService.getAllNotices());
    }

    /**
     * FR12: Bulk document assembly and contract packet generation
     */
    @GetMapping("/{id}/packet")
    public ResponseEntity<?> getContractPacket(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(documentService.assembleContractPacket(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
