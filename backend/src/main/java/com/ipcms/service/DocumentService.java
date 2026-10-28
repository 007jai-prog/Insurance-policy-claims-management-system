package com.ipcms.service;

import com.ipcms.model.Policy;
import com.ipcms.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentService {

    private final PolicyRepository policyRepository;

    public DocumentService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * FR12: Assembles standardized contract packet with contextual riders based on geographic rules.
     * Generates a unique receipt tracking number linked to the customer profile.
     */
    public Map<String, Object> assembleContractPacket(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        String uniqueReceiptNumber = "DOC-RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());

        Map<String, Object> packet = new HashMap<>();
        packet.put("receiptNumber", uniqueReceiptNumber);
        packet.put("policyNumber", policy.getPolicyNumber());
        packet.put("policyholderName", policy.getPolicyholder().getFullName());
        packet.put("policyholderEmail", policy.getPolicyholder().getEmail());
        packet.put("productType", policy.getProductType());
        packet.put("coverageLimit", policy.getCoverageLimit());
        packet.put("basePremium", policy.getBasePremium());
        packet.put("effectiveDate", policy.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        packet.put("expiryDate", policy.getExpiryDate().toString());
        packet.put("complianceStatus", "ACORD_STANDARDIZED");

        // Contextual geographic riders (FR12)
        String[] riders;
        if (policy.getRegionRiskIndex() != null && policy.getRegionRiskIndex() > 1.2) {
            riders = new String[]{
                "RIDER-CAT-01: High-Frequency Catastrophic Weather Extended Endorsement",
                "RIDER-GEO-04: Coastal Storm Surge Mitigation & Deductible Waiver",
                "RIDER-LEGAL-09: Mandatory State Insurance Regulatory Board Disclosure"
            };
        } else {
            riders = new String[]{
                "RIDER-STD-01: Standard Commercial & Retail Liability Endorsement",
                "RIDER-LEGAL-09: Mandatory State Insurance Regulatory Board Disclosure"
            };
        }
        packet.put("appendedRiders", riders);
        packet.put("assembledAt", LocalDateTime.now().toString());

        return packet;
    }

    /**
     * Appendix E: ACORD System Exchange Standard Schema format
     */
    public Map<String, Object> generateAcordMessage(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        Map<String, Object> acord = new HashMap<>();
        acord.put("ACORDStandardVersion", "TXLife2.38.00 / ACORD 125");
        acord.put("MessageId", UUID.randomUUID().toString());
        acord.put("TransmissionTimestamp", LocalDateTime.now().toString());

        Map<String, Object> insResponse = new HashMap<>();
        insResponse.put("PolicyNumber", policy.getPolicyNumber());
        insResponse.put("LineOfBusiness", policy.getProductType());
        insResponse.put("TotalPremiumAmount", policy.getBasePremium());
        insResponse.put("ActiveReserveRequirement", policy.getActiveReserve());
        insResponse.put("InsuredName", policy.getPolicyholder().getFullName());
        insResponse.put("CarrierOfficeId", policy.getPolicyholder().getOfficeId() != null ? policy.getPolicyholder().getOfficeId() : "HQ-EAST-01");

        acord.put("InsuranceSvcRs", insResponse);
        return acord;
    }
}
