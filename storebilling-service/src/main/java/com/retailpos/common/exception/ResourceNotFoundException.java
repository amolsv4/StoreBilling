package com.retailpos.common.exception;

/**
 * Thrown when a requested resource does not exist. Mapped to HTTP 404 by
 * {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forEntity(String entityName, Object identifier) {
        return new ResourceNotFoundException(entityName + " not found for identifier: " + identifier);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
