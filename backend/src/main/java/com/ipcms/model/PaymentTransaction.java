package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 50)
    private String transactionRef;

    // Sub-ledger itemization (FR7)
    @Column(name = "base_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "admin_fees", nullable = false, precision = 15, scale = 2)
    private BigDecimal adminFees;

    @Column(name = "processing_tax", nullable = false, precision = 15, scale = 2)
    private BigDecimal processingTax;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_channel", length = 40)
    private String paymentChannel = "DIRECT_DEBIT"; // DIRECT_DEBIT, ACH, CREDIT_CARD

    @Column(name = "status", length = 30)
    private String status = "SETTLED"; // SETTLED, FAILED, GRACE_PERIOD

    @Column(name = "grace_period_active")
    private Boolean gracePeriodActive = false;

    @Column(name = "grace_period_end_date")
    private LocalDate gracePeriodEndDate;

    @Column(name = "processed_at")
    private LocalDateTime processedAt = LocalDateTime.now();

    public PaymentTransaction() {}

    public PaymentTransaction(Policy policy, String transactionRef, BigDecimal baseCost, BigDecimal adminFees, BigDecimal processingTax, String paymentChannel, String status) {
        this.policy = policy;
        this.transactionRef = transactionRef;
        this.baseCost = baseCost;
        this.adminFees = adminFees;
        this.processingTax = processingTax;
        this.totalAmount = baseCost.add(adminFees).add(processingTax);
        this.paymentChannel = paymentChannel;
        this.status = status;
        this.gracePeriodActive = "FAILED".equals(status) || "GRACE_PERIOD".equals(status);
        if (this.gracePeriodActive) {
            this.gracePeriodEndDate = LocalDate.now().plusDays(30);
        }
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public BigDecimal getBaseCost() { return baseCost; }
    public void setBaseCost(BigDecimal baseCost) { this.baseCost = baseCost; }

    public BigDecimal getAdminFees() { return adminFees; }
    public void setAdminFees(BigDecimal adminFees) { this.adminFees = adminFees; }

    public BigDecimal getProcessingTax() { return processingTax; }
    public void setProcessingTax(BigDecimal processingTax) { this.processingTax = processingTax; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentChannel() { return paymentChannel; }
    public void setPaymentChannel(String paymentChannel) { this.paymentChannel = paymentChannel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getGracePeriodActive() { return gracePeriodActive; }
    public void setGracePeriodActive(Boolean gracePeriodActive) { this.gracePeriodActive = gracePeriodActive; }

    public LocalDate getGracePeriodEndDate() { return gracePeriodEndDate; }
    public void setGracePeriodEndDate(LocalDate gracePeriodEndDate) { this.gracePeriodEndDate = gracePeriodEndDate; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
