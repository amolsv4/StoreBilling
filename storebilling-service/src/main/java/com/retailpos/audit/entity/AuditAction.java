package com.retailpos.audit.entity;

/** The action recorded in an {@link AuditLog} entry. */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT
}
