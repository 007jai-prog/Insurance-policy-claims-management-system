package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "endorsements")
public class Endorsement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "endorsement_number", nullable = false, unique = true, length = 40)
    private String endorsementNumber;

    @Column(name = "alteration_type", nullable = false, length = 60)
    private String alterationType; // COVERAGE_INCREASE, ADD_RIDER, DEDUCTIBLE_ALTERATION, ASSET_UPDATE

    @Column(name = "pro_rata_delta", precision = 15, scale = 2)
    private BigDecimal proRataDelta;

    @Column(name = "new_premium", precision = 15, scale = 2)
    private BigDecimal newPremium;

    @Column(name = "historical_configuration", length = 3000)
    private String historicalConfiguration; // Archived JSON snapshot of previous terms

    @Column(name = "justification_notes", length = 1000)
    private String justificationNotes;

    @Column(name = "processed_at")
    private LocalDateTime processedAt = LocalDateTime.now();

    public Endorsement() {}

    public Endorsement(Policy policy, String endorsementNumber, String alterationType, BigDecimal proRataDelta, BigDecimal newPremium, String historicalConfiguration, String justificationNotes) {
        this.policy = policy;
        this.endorsementNumber = endorsementNumber;
        this.alterationType = alterationType;
        this.proRataDelta = proRataDelta;
        this.newPremium = newPremium;
        this.historicalConfiguration = historicalConfiguration;
        this.justificationNotes = justificationNotes;
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public String getEndorsementNumber() { return endorsementNumber; }
    public void setEndorsementNumber(String endorsementNumber) { this.endorsementNumber = endorsementNumber; }

    public String getAlterationType() { return alterationType; }
    public void setAlterationType(String alterationType) { this.alterationType = alterationType; }

    public BigDecimal getProRataDelta() { return proRataDelta; }
    public void setProRataDelta(BigDecimal proRataDelta) { this.proRataDelta = proRataDelta; }

    public BigDecimal getNewPremium() { return newPremium; }
    public void setNewPremium(BigDecimal newPremium) { this.newPremium = newPremium; }

    public String getHistoricalConfiguration() { return historicalConfiguration; }
    public void setHistoricalConfiguration(String historicalConfiguration) { this.historicalConfiguration = historicalConfiguration; }

    public String getJustificationNotes() { return justificationNotes; }
    public void setJustificationNotes(String justificationNotes) { this.justificationNotes = justificationNotes; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
