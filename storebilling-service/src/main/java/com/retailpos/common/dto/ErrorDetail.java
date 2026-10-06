package com.retailpos.common.dto;

/**
 * A single field-level (or general) error item, used inside {@link ApiResponse#getErrors()}.
 *
 * @param field   the offending field/parameter name, or {@code null} for a non-field-specific error
 * @param message a human-readable description of the problem
 */
public record ErrorDetail(String field, String message) {
}
