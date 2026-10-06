package com.retailpos.common.exception;

/**
 * Thrown when an operation would violate a uniqueness constraint (e.g. a duplicate code/username).
 * Mapped to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {

    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    public DuplicateResourceException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
