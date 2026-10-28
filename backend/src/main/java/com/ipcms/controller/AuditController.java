package com.ipcms.controller;

import com.ipcms.model.AuditLog;
import com.ipcms.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * FR11: Regulatory Audit Trail and Disclosure Compilers
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<List<AuditLog>> getEntityLogs(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getLogsForEntity(entityName, entityId));
    }
}
