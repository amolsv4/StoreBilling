package com.retailpos.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard response envelope used by every controller in the application:
 *
 * <pre>{@code
 * { "success": true,  "message": "...", "data": {} }
 * { "success": false, "message": "...", "errorCode": "...", "errors": [] }
 * }</pre>
 *
 * @param <T> the payload type carried on success
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final List<ErrorDetail> errors;

    private ApiResponse(boolean success, String message, T data, String errorCode, List<ErrorDetail> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> failure(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, null);
    }

    public static <T> ApiResponse<T> failure(String message, String errorCode, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, message, null, errorCode, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}
