package com.ipcms.controller;

import com.ipcms.dto.AdjusterAssignRequest;
import com.ipcms.dto.ClaimFnolRequest;
import com.ipcms.dto.ReserveUpdateRequest;
import com.ipcms.dto.SubrogationRequest;
import com.ipcms.model.*;
import com.ipcms.repository.ClaimRepository;
import com.ipcms.repository.UserRepository;
import com.ipcms.service.AdjusterService;
import com.ipcms.service.ClaimsService;
import com.ipcms.service.SubrogationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimsController {

    private final ClaimsService claimsService;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final SubrogationService subrogationService;
    private final AdjusterService adjusterService;

    public ClaimsController(
            ClaimsService claimsService,
            ClaimRepository claimRepository,
            UserRepository userRepository,
            SubrogationService subrogationService,
            AdjusterService adjusterService) {
        this.claimsService = claimsService;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.subrogationService = subrogationService;
        this.adjusterService = adjusterService;
    }

    /**
     * Appendix H: POST /api/claims/fnol
     * Registers incoming incident files instantly
     */
    @PostMapping("/fnol")
    public ResponseEntity<?> fileFnol(@Valid @RequestBody ClaimFnolRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        try {
            Claim claim = claimsService.registerFnol(request, username);
            return ResponseEntity.ok(claim);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Appendix H: PUT /api/claims/reserve/{id}
     * Updates active financial reserve metrics manually (Appendix A: Handlers and Admins)
     */
    @PutMapping("/reserve/{id}")
    public ResponseEntity<?> updateReserve(@PathVariable Long id, @Valid @RequestBody ReserveUpdateRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        String role = authentication != null && !authentication.getAuthorities().isEmpty()
                ? authentication.getAuthorities().iterator().next().getAuthority() : "ROLE_HANDLER";
        try {
            Claim updated = claimsService.updateReserve(id, request, username, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Claim>> getAllClaims(Authentication authentication) {
        if (authentication == null) return ResponseEntity.ok(List.of());

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null && user.getRole() == Role.HOLDER) {
            return ResponseEntity.ok(claimsService.getClaimsByReporter(user.getId()));
        }
        return ResponseEntity.ok(claimsService.getAllClaims());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Claim>> getMyClaims(Authentication authentication) {
        if (authentication == null) return ResponseEntity.ok(List.of());
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(claimsService.getClaimsByReporter(user.getId()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateClaimStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication authentication) {
        String status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(Map.of("error", "status parameter required"));
        String username = authentication != null ? authentication.getName() : "system";
        String role = authentication != null && !authentication.getAuthorities().isEmpty()
                ? authentication.getAuthorities().iterator().next().getAuthority() : "ROLE_HANDLER";

        try {
            Claim updated = claimsService.updateStatus(id, status, username, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * FR6: Subrogation & Co-insurance tracking
     */
    @PostMapping("/subrogation")
    public ResponseEntity<?> createSubrogation(@Valid @RequestBody SubrogationRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        try {
            SubrogationCase sc = subrogationService.createSubrogation(request, username);
            return ResponseEntity.ok(sc);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/subrogation/all")
    public ResponseEntity<List<SubrogationCase>> getAllSubrogationCases() {
        return ResponseEntity.ok(subrogationService.getAllCases());
    }

    @PostMapping("/subrogation/{caseId}/recover")
    public ResponseEntity<?> recordRecovery(@PathVariable Long caseId, @RequestBody Map<String, BigDecimal> payload, Authentication authentication) {
        BigDecimal amount = payload.get("amount");
        if (amount == null) return ResponseEntity.badRequest().body(Map.of("error", "amount is required"));
        String username = authentication != null ? authentication.getName() : "system";
        return ResponseEntity.ok(subrogationService.recordRecovery(caseId, amount, username));
    }

    /**
     * FR9: Field Adjuster Assignment Routing
     */
    @PostMapping("/adjuster/dispatch")
    public ResponseEntity<?> dispatchAdjuster(@Valid @RequestBody AdjusterAssignRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        try {
            AdjusterAssignment assignment = adjusterService.dispatchAdjuster(request, username);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/adjuster/all")
    public ResponseEntity<List<AdjusterAssignment>> getAllAdjusterAssignments() {
        return ResponseEntity.ok(adjusterService.getAllAssignments());
    }

    @PostMapping("/adjuster/{assignmentId}/report")
    public ResponseEntity<?> submitAdjusterReport(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, Object> body) {
        BigDecimal valuation = new BigDecimal(body.get("valuation").toString());
        String notes = (String) body.get("notes");
        String signatureHash = (String) body.getOrDefault("signatureHash", "SIG-" + System.currentTimeMillis());

        AdjusterAssignment assignment = adjusterService.submitValuationReport(assignmentId, valuation, notes, signatureHash);
        return ResponseEntity.ok(assignment);
    }
}
