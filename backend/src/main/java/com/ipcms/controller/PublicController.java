package com.ipcms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * Appendix A: Browse Products (Available to GUEST, HOLDER, AGENT, HANDLER, UNDERWRITER, ADMIN)
     */
    @GetMapping("/products")
    public ResponseEntity<?> getPublicProducts() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "PROD-AUTO",
                        "title", "Commercial & Fleet Auto Comprehensive",
                        "code", "AUTO",
                        "category", "Vehicular Transport",
                        "description", "End-to-end collision, cargo liability, telematics accident mitigation, and 24/7 roadside subrogation.",
                        "startingRate", "$120/mo",
                        "coverageMax", "$1,000,000",
                        "standardRiders", List.of("Fleet Gap Protection", "Roadside Expedited Towing", "Rental Reimbursement")
                ),
                Map.of(
                        "id", "PROD-PROP",
                        "title", "Commercial Real Estate & Physical Asset Protection",
                        "code", "PROP",
                        "category", "Property & Infrastructure",
                        "description", "Protects physical structures, industrial facilities, warehouses, and loss of business income against catastrophes.",
                        "startingRate", "$240/mo",
                        "coverageMax", "$5,000,000",
                        "standardRiders", List.of("Catastrophic Weather Endorsement", "Business Interruption Recovery", "Equipment Breakdown")
                ),
                Map.of(
                        "id", "PROD-HLTH",
                        "title", "Enterprise Executive Health & Group Medical Elite",
                        "code", "HLTH",
                        "category", "Health & Life Benefits",
                        "description", "Comprehensive medical, specialist consultation, mental wellness, prescription formulary, and global repatriation.",
                        "startingRate", "$180/mo",
                        "coverageMax", "$2,000,000",
                        "standardRiders", List.of("Executive Dental & Vision", "Global Emergency Evacuation", "Zero Deductible Wellness")
                ),
                Map.of(
                        "id", "PROD-CYBR",
                        "title", "Corporate Cyber Shield & Data Breach Indemnity",
                        "code", "CYBR",
                        "category", "Cyber & Technology Risk",
                        "description", "First-party ransom indemnity, forensic incident response, regulatory disclosure defense, and GDPR/CCPA fine protection.",
                        "startingRate", "$310/mo",
                        "coverageMax", "$10,000,000",
                        "standardRiders", List.of("Forensic Investigator Retainer", "Ransomware Settlement Allocation", "Public Relations Defense")
                )
        ));
    }
}
