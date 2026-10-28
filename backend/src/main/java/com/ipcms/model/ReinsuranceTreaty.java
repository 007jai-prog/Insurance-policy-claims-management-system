package com.ipcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reinsurance_treaties")
public class ReinsuranceTreaty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "treaty_name", nullable = false, unique = true, length = 100)
    private String treatyName;

    @Column(name = "treaty_type", nullable = false, length = 50)
    private String treatyType; // EXCESS_OF_LOSS, QUOTA_SHARE, FACULTATIVE

    @Column(name = "capacity_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal capacityLimit;

    @Column(name = "retention_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal retentionLimit; // Threshold above which reinsurance triggers

    @Column(name = "utilized_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal utilizedAmount = BigDecimal.ZERO;

    @Column(name = "active_year", nullable = false)
    private Integer activeYear = 2026;

    public ReinsuranceTreaty() {}

    public ReinsuranceTreaty(String treatyName, String treatyType, BigDecimal capacityLimit, BigDecimal retentionLimit, BigDecimal utilizedAmount, Integer activeYear) {
        this.treatyName = treatyName;
        this.treatyType = treatyType;
        this.capacityLimit = capacityLimit;
        this.retentionLimit = retentionLimit;
        this.utilizedAmount = utilizedAmount != null ? utilizedAmount : BigDecimal.ZERO;
        this.activeYear = activeYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTreatyName() { return treatyName; }
    public void setTreatyName(String treatyName) { this.treatyName = treatyName; }

    public String getTreatyType() { return treatyType; }
    public void setTreatyType(String treatyType) { this.treatyType = treatyType; }

    public BigDecimal getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(BigDecimal capacityLimit) { this.capacityLimit = capacityLimit; }

    public BigDecimal getRetentionLimit() { return retentionLimit; }
    public void setRetentionLimit(BigDecimal retentionLimit) { this.retentionLimit = retentionLimit; }

    public BigDecimal getUtilizedAmount() { return utilizedAmount; }
    public void setUtilizedAmount(BigDecimal utilizedAmount) { this.utilizedAmount = utilizedAmount; }

    public Integer getActiveYear() { return activeYear; }
    public void setActiveYear(Integer activeYear) { this.activeYear = activeYear; }

    @Transient
    public BigDecimal getRemainingCapacity() {
        return capacityLimit.subtract(utilizedAmount);
    }
}
