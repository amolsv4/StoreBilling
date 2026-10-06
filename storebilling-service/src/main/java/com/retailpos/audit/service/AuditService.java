package com.retailpos.audit.service;

import com.retailpos.audit.entity.AuditAction;
import com.retailpos.audit.entity.AuditLog;
import com.retailpos.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Simple append-only audit writer used by the auth and configuration modules. Kept as a single
 * concrete class (no interface split) since its responsibility is a single, simple "write a row"
 * operation - unlike the CRUD modules, there's no alternate implementation to swap in.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Writes an audit row in its own new transaction, so a failure recording an audit entry (or
     * a rollback of the surrounding business transaction, e.g. a failed login) never prevents the
     * entry itself from being persisted.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String entityName, Long entityId, AuditAction action, String performedByUsername,
                        String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .performedByUsername(performedByUsername)
                .details(details)
                .ipAddress(ipAddress)
                .createdDate(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public void recordLoginSuccess(String username, String ipAddress) {
        record("app_user", null, AuditAction.LOGIN_SUCCESS, username, "Login succeeded.", ipAddress);
    }

    public void recordLoginFailure(String username, String ipAddress, String reason) {
        record("app_user", null, AuditAction.LOGIN_FAILURE, username, reason, ipAddress);
    }

    public void recordLogout(String username, String ipAddress) {
        record("app_user", null, AuditAction.LOGOUT, username, "Logout.", ipAddress);
    }
}
