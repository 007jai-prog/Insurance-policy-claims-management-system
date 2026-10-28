package com.ipcms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 60)
    private String entityName; // POLICY, CLAIM, RESERVE, USER, UNDERWRITING

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 40)
    private String action; // CREATE, UPDATE_RESERVE, OVERRIDE_UNDERWRITE, REJECTION, FRAUD_FREEZE

    @Column(name = "changed_by", nullable = false, length = 60)
    private String changedBy;

    @Column(name = "role_used", length = 30)
    private String roleUsed;

    @Column(name = "change_summary", nullable = false, length = 2000)
    private String changeSummary;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String entityName, Long entityId, String action, String changedBy, String roleUsed, String changeSummary) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.changedBy = changedBy;
        this.roleUsed = roleUsed;
        this.changeSummary = changeSummary;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getRoleUsed() { return roleUsed; }
    public void setRoleUsed(String roleUsed) { this.roleUsed = roleUsed; }

    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
