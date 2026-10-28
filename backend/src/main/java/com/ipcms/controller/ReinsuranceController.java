package com.ipcms.controller;

import com.ipcms.model.ReinsuranceTreaty;
import com.ipcms.service.ReinsuranceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reinsurance")
public class ReinsuranceController {

    private final ReinsuranceService reinsuranceService;

    public ReinsuranceController(ReinsuranceService reinsuranceService) {
        this.reinsuranceService = reinsuranceService;
    }

    @GetMapping("/treaties")
    public ResponseEntity<List<ReinsuranceTreaty>> getAllTreaties() {
        return ResponseEntity.ok(reinsuranceService.getAllTreaties());
    }

    @PostMapping("/evaluate/{claimId}")
    public ResponseEntity<?> evaluateTreatyAllocation(@PathVariable Long claimId) {
        try {
            String result = reinsuranceService.evaluateTreatyAllocation(claimId);
            return ResponseEntity.ok(Map.of("allocationResult", result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
