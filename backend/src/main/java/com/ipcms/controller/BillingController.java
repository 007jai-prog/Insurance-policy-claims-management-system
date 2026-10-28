package com.ipcms.controller;

import com.ipcms.model.PaymentTransaction;
import com.ipcms.model.Role;
import com.ipcms.model.User;
import com.ipcms.repository.UserRepository;
import com.ipcms.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final UserRepository userRepository;

    public BillingController(BillingService billingService, UserRepository userRepository) {
        this.billingService = billingService;
        this.userRepository = userRepository;
    }

    /**
     * FR7: Direct debit payment creation with itemized sub-ledgers and grace-period logic
     */
    @PostMapping("/invoice/{policyId}")
    public ResponseEntity<?> createInvoice(
            @PathVariable Long policyId,
            @RequestParam(defaultValue = "false") boolean simulateFailure) {
        try {
            PaymentTransaction transaction = billingService.createDirectDebitInvoice(policyId, simulateFailure);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PaymentTransaction>> getPolicyTransactions(@PathVariable Long policyId) {
        return ResponseEntity.ok(billingService.getPolicyPayments(policyId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentTransaction>> getMyTransactions(Authentication authentication) {
        if (authentication == null) return ResponseEntity.ok(List.of());
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(billingService.getUserPayments(user.getId()));
    }
}
