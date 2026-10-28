package com.ipcms.controller;

import com.ipcms.dto.UnderwritingOverrideRequest;
import com.ipcms.model.Policy;
import com.ipcms.service.UnderwritingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/underwriting")
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    public UnderwritingController(UnderwritingService underwritingService) {
        this.underwritingService = underwritingService;
    }

    /**
     * FR2: Rating Matrix rate calculation formula
     */
    @PostMapping("/calculate-rate")
    public ResponseEntity<?> calculateRate(@RequestBody Map<String, Object> payload) {
        String productType = (String) payload.getOrDefault("productType", "AUTO COMPREHENSIVE");
        BigDecimal assetValue = new BigDecimal(payload.getOrDefault("assetValue", "50000").toString());
        Double regionRiskIndex = Double.parseDouble(payload.getOrDefault("regionRiskIndex", "1.15").toString());
        Integer applicantAge = Integer.parseInt(payload.getOrDefault("applicantAge", "35").toString());
        Integer pastClaims = Integer.parseInt(payload.getOrDefault("pastClaims", "0").toString());

        BigDecimal calculated = underwritingService.calculatePremium(productType, assetValue, regionRiskIndex, applicantAge, pastClaims);

        return ResponseEntity.ok(Map.of(
                "productType", productType,
                "assetValue", assetValue,
                "regionRiskIndex", regionRiskIndex,
                "applicantAge", applicantAge,
                "pastClaims", pastClaims,
                "calculatedAnnualPremium", calculated,
                "formulaVersion", "ACTUARIAL-RMS-2026.3"
        ));
    }

    /**
     * FR2 & Appendix A: Manual Underwriter Intervention requiring supervisor sign-off
     */
    @PostMapping("/override")
    public ResponseEntity<?> overrideUnderwrite(
            @Valid @RequestBody UnderwritingOverrideRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "underwriter";
        try {
            Policy updated = underwritingService.overrideUnderwrite(request, username);
            return ResponseEntity.ok(updated);
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getActuarialTemplates() {
        return ResponseEntity.ok(List.of(
                Map.of("tier", "TIER_1_PRIME", "lossRatioThreshold", 0.42, "regionalMultiplier", 1.05, "desc", "Low-density suburban exposure"),
                Map.of("tier", "TIER_2_STANDARD", "lossRatioThreshold", 0.58, "regionalMultiplier", 1.18, "desc", "Metropolitan commercial perimeter"),
                Map.of("tier", "TIER_3_HIGH_EXPOSURE", "lossRatioThreshold", 0.76, "regionalMultiplier", 1.45, "desc", "Coastal flood / high industrial volatility")
        ));
    }
}
