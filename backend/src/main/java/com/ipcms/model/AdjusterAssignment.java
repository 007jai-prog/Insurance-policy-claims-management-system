package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "adjuster_assignments")
public class AdjusterAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "adjuster_name", nullable = false, length = 100)
    private String adjusterName;

    @Column(name = "specialty", nullable = false, length = 60)
    private String specialty; // AUTOMOTIVE_STRUCTURAL, COMMERCIAL_PROPERTY, INDUSTRIAL_MACHINERY, CASUALTY

    @Column(name = "structural_load_limit", precision = 15, scale = 2)
    private BigDecimal structuralLoadLimit; // Max authorization limit

    @Column(name = "field_imagery_url", length = 300)
    private String fieldImageryUrl;

    @Column(name = "valuation_amount", precision = 15, scale = 2)
    private BigDecimal valuationAmount;

    @Column(name = "inspection_notes", length = 1500)
    private String inspectionNotes;

    @Column(name = "digital_signature_hash", length = 120)
    private String digitalSignatureHash;

    @Column(name = "status", length = 30)
    private String status = "DISPATCHED"; // DISPATCHED, ON_SITE, VALUATION_SUBMITTED, COMPLETED

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    public AdjusterAssignment() {}

    public AdjusterAssignment(Claim claim, String adjusterName, String specialty, BigDecimal structuralLoadLimit) {
        this.claim = claim;
        this.adjusterName = adjusterName;
        this.specialty = specialty;
        this.structuralLoadLimit = structuralLoadLimit;
        this.status = "DISPATCHED";
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public String getAdjusterName() { return adjusterName; }
    public void setAdjusterName(String adjusterName) { this.adjusterName = adjusterName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public BigDecimal getStructuralLoadLimit() { return structuralLoadLimit; }
    public void setStructuralLoadLimit(BigDecimal structuralLoadLimit) { this.structuralLoadLimit = structuralLoadLimit; }

    public String getFieldImageryUrl() { return fieldImageryUrl; }
    public void setFieldImageryUrl(String fieldImageryUrl) { this.fieldImageryUrl = fieldImageryUrl; }

    public BigDecimal getValuationAmount() { return valuationAmount; }
    public void setValuationAmount(BigDecimal valuationAmount) { this.valuationAmount = valuationAmount; }

    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public String getDigitalSignatureHash() { return digitalSignatureHash; }
    public void setDigitalSignatureHash(String digitalSignatureHash) { this.digitalSignatureHash = digitalSignatureHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
