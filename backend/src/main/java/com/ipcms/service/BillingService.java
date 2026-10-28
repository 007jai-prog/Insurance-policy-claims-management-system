package com.ipcms.service;

import com.ipcms.model.PaymentTransaction;
import com.ipcms.model.Policy;
import com.ipcms.repository.PaymentTransactionRepository;
import com.ipcms.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private final PaymentTransactionRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public BillingService(PaymentTransactionRepository paymentRepository, PolicyRepository policyRepository, AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.auditService = auditService;
    }

    /**
     * FR7: Premium sub-ledger itemizing processing taxes (8.25%), administrative line items ($35), and base costs.
     */
    @Transactional
    public PaymentTransaction createDirectDebitInvoice(Long policyId, boolean simulateFailure) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        BigDecimal baseCost = policy.getBasePremium();
        BigDecimal adminFees = new BigDecimal("35.00");
        BigDecimal processingTax = baseCost.multiply(new BigDecimal("0.0825")).setScale(2, RoundingMode.HALF_UP);

        String ref = "TXN-DD-" + System.currentTimeMillis() + "-" + (random.nextInt(900) + 100);
        String initialStatus = simulateFailure ? "FAILED" : "SETTLED";

        PaymentTransaction transaction = new PaymentTransaction(
                policy,
                ref,
                baseCost,
                adminFees,
                processingTax,
                "DIRECT_DEBIT",
                initialStatus
        );

        if (simulateFailure) {
            transaction.setGracePeriodActive(true);
            transaction.setGracePeriodEndDate(LocalDate.now().plusDays(30));
        }

        PaymentTransaction saved = paymentRepository.save(transaction);

        auditService.log(
                "BILLING",
                policy.getId(),
                simulateFailure ? "DEBIT_FAILED_GRACE_PERIOD" : "PAYMENT_SETTLED",
                "CLEARINGHOUSE",
                "AUTOMATION",
                "Direct debit invoice " + ref + " created. Base: $" + baseCost + ", Admin: $" + adminFees +
                ", Tax: $" + processingTax + ", Total: $" + transaction.getTotalAmount() + ". Status: " + initialStatus +
                (simulateFailure ? " [30-Day Grace Period Initiated]" : "")
        );

        return saved;
    }

    public List<PaymentTransaction> getPolicyPayments(Long policyId) {
        return paymentRepository.findByPolicyId(policyId);
    }

    public List<PaymentTransaction> getUserPayments(Long userId) {
        return paymentRepository.findByPolicy_Policyholder_Id(userId);
    }
}
