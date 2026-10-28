package com.ipcms.config;

import com.ipcms.model.*;
import com.ipcms.repository.*;
import com.ipcms.service.AuditService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ReinsuranceTreatyRepository treatyRepository;
    private final SubrogationCaseRepository subrogationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DataInitializer(
            UserRepository userRepository,
            PolicyRepository policyRepository,
            ClaimRepository claimRepository,
            ReinsuranceTreatyRepository treatyRepository,
            SubrogationCaseRepository subrogationRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.treatyRepository = treatyRepository;
        this.subrogationRepository = subrogationRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already populated
        }

        System.out.println(">>> Initializing IPCMS Enterprise Baseline Data & Role Hierarchy...");

        // 1. Seed Appendix A Roles
        // Admin
        User admin = new User("admin", "admin@ipcms.enterprise.com", passwordEncoder.encode("admin123"), Role.ADMIN, "Dr. Arthur Vance (Chief Compliance & Admin)", "9876543210", "TAX-US-ADM-001");
        admin.setOfficeId("HQ-EXEC-01");
        admin.setCertificationNumber("CERT-ADM-8891");
        userRepository.save(admin);

        // Underwriter
        User underwriter = new User("underwriter", "underwriter@ipcms.enterprise.com", passwordEncoder.encode("underwriter123"), Role.UNDERWRITER, "Elena Rostova (Lead Actuarial Underwriter)", "9876543211", "TAX-US-UND-002");
        underwriter.setOfficeId("HQ-ACT-04");
        underwriter.setCertificationNumber("ACORD-FELLOW-1102");
        userRepository.save(underwriter);

        // Claims Handler
        User handler = new User("handler", "handler@ipcms.enterprise.com", passwordEncoder.encode("handler123"), Role.HANDLER, "Marcus Brody (Senior Adjudication Examiner)", "9876543212", "TAX-US-HDL-003");
        handler.setOfficeId("REG-EAST-CLAIMS");
        handler.setCertificationNumber("AIC-CLAIMS-4421");
        userRepository.save(handler);

        // Insurance Agent
        User agent = new User("agent", "agent@ipcms.enterprise.com", passwordEncoder.encode("agent123"), Role.AGENT, "Samantha Reed (Commercial Lines Broker)", "9876543213", "TAX-US-AGT-004");
        agent.setOfficeId("METRO-BRANCH-12");
        agent.setCertificationNumber("NPN-BROKER-9982");
        userRepository.save(agent);

        // Policyholder
        User holder = new User("holder", "policyholder@corporate.com", passwordEncoder.encode("holder123"), Role.HOLDER, "Jonathan Hayes (Apex Logistics Corp)", "9876543214", "TAX-CORP-48821");
        userRepository.save(holder);

        // 2. Seed Baseline Policies (Appendix B & C pattern: VARCHAR(30) UNIQUE NOT NULL)
        Policy autoPolicy = new Policy(
                "AUTO-10042",
                holder,
                "Auto Comprehensive",
                new BigDecimal("1450.00"),
                new BigDecimal("250000.00"),
                LocalDate.now().plusMonths(10),
                "ACTIVE",
                "VIN-1HGCR2F83HA02910"
        );
        autoPolicy.setRegionRiskIndex(1.15);
        policyRepository.save(autoPolicy);

        Policy propPolicy = new Policy(
                "PROP-98120",
                holder,
                "Commercial Property",
                new BigDecimal("4800.00"),
                new BigDecimal("2500000.00"),
                LocalDate.now().plusDays(28), // Expiring in 28 days (< 45 days for FR5 renewal trigger test!)
                "ACTIVE",
                "BLDG-LOGISTICS-HUB-B"
        );
        propPolicy.setRegionRiskIndex(1.35);
        propPolicy.setActiveReserve(new BigDecimal("12500.00"));
        policyRepository.save(propPolicy);

        Policy cyberPolicy = new Policy(
                "CYBR-77312",
                holder,
                "Cyber Shield",
                new BigDecimal("6200.00"),
                new BigDecimal("5000000.00"),
                LocalDate.now().plusMonths(8),
                "ACTIVE",
                "DC-CLOUD-CONTAINER-CLUSTER-EAST"
        );
        cyberPolicy.setRegionRiskIndex(1.40);
        policyRepository.save(cyberPolicy);

        Policy hlthPolicy = new Policy(
                "HLTH-55201",
                holder,
                "Health Elite",
                new BigDecimal("2100.00"),
                new BigDecimal("1000000.00"),
                LocalDate.now().plusMonths(11),
                "ACTIVE",
                "GRP-BENEFITS-ROSTER-2026"
        );
        hlthPolicy.setRegionRiskIndex(1.05);
        policyRepository.save(hlthPolicy);

        // 3. Seed Sample Claim (Appendix B Constraints: gross_reserve DECIMAL(15,2) default 0.00)
        Claim propClaim = new Claim(
                "CLM-2026-10492",
                propPolicy,
                holder,
                LocalDate.now().minusDays(5),
                new BigDecimal("12500.00"),
                "Localized electrical fire in Warehouse Sector 4 causing structural roof scorch and partial HVAC intake damage.",
                "Warehouse Sector 4, Newark Hub, NJ",
                new BigDecimal("18500.00")
        );
        propClaim.setStatus("UNDER_INVESTIGATION");
        propClaim.setWitnessStatement("Shift supervisor observed breaker panel spark prior to fire suppression activation.");
        propClaim.setSlaDeadline(LocalDate.now().plusDays(9)); // Remaining SLA days
        claimRepository.save(propClaim);

        // 4. Seed Subrogation Case (FR6)
        SubrogationCase subCase = new SubrogationCase(
                propClaim,
                "Continental Industrial Underwriters Ltd",
                75,
                new BigDecimal("9375.00"),
                new BigDecimal("25.00")
        );
        subCase.setRecoveredAmount(new BigDecimal("2500.00"));
        subCase.setStatus("DEMAND_ISSUED");
        subrogationRepository.save(subCase);

        // 5. Seed Reinsurance Treaties (FR10)
        ReinsuranceTreaty treaty1 = new ReinsuranceTreaty(
                "Global Atlantic Catastrophe Excess-Loss Treaty",
                "EXCESS_OF_LOSS",
                new BigDecimal("50000000.00"),
                new BigDecimal("500000.00"), // Retention limit $500k
                new BigDecimal("4200000.00"),
                2026
        );
        treatyRepository.save(treaty1);

        ReinsuranceTreaty treaty2 = new ReinsuranceTreaty(
                "Pacific Maritime & Commercial Quota-Share Treaty",
                "QUOTA_SHARE",
                new BigDecimal("25000000.00"),
                new BigDecimal("250000.00"),
                new BigDecimal("1800000.00"),
                2026
        );
        treatyRepository.save(treaty2);

        // 6. Seed Baseline Regulatory Audit Logs (FR11)
        auditService.log("SYSTEM", 1L, "SYSTEM_INITIALIZATION", "admin", "ADMIN", "IPCMS Enterprise baseline entities, role hierarchy, and ledger tables initialized");
        auditService.log("POLICY", autoPolicy.getId(), "ISSUE_POLICY", "underwriter", "UNDERWRITER", "Policy AUTO-10042 bound and issued under RMS-2026 actuarial rules");
        auditService.log("CLAIM", propClaim.getId(), "FNOL_RECORDED", "holder", "HOLDER", "First Notice of Loss CLM-2026-10492 filed with initial gross reserve $12,500.00");

        System.out.println(">>> IPCMS Enterprise Baseline Data successfully populated!");
    }
}
