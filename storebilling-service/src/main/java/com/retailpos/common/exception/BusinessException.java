package com.retailpos.common.exception;

/**
 * Generic business-rule violation that doesn't fit {@link ResourceNotFoundException} or
 * {@link DuplicateResourceException}. Mapped to HTTP 400 by {@link GlobalExceptionHandler}.
 */
public class BusinessException extends RuntimeException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = DEFAULT_ERROR_CODE;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
