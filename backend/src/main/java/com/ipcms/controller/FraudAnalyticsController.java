package com.ipcms.controller;

import com.ipcms.dto.FraudReviewRequest;
import com.ipcms.model.FraudAlert;
import com.ipcms.service.FraudService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fraud")
public class FraudAnalyticsController {

    private final FraudService fraudService;

    public FraudAnalyticsController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlert>> getAllAlerts() {
        return ResponseEntity.ok(fraudService.getAllAlerts());
    }

    @PostMapping("/evaluate/{claimId}")
    public ResponseEntity<?> triggerAssessment(@PathVariable Long claimId) {
        FraudAlert alert = fraudService.triggerRiskAssessment(claimId);
        if (alert != null) {
            return ResponseEntity.ok(alert);
        }
        return ResponseEntity.ok(Map.of("message", "Claim risk score evaluated below threshold; no anomaly alert triggered."));
    }

    @PutMapping("/review/{alertId}")
    public ResponseEntity<?> reviewAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody FraudReviewRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "security_analyst";
        try {
            FraudAlert updated = fraudService.reviewAlert(alertId, request, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
