package com.internship.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform error response body returned by GlobalExceptionHandler for every error.
 * <p>
 * JSON shape:
 * <pre>
 * {
 *   "timestamp":  "2026-04-30T10:00:00",
 *   "status":     404,
 *   "errorCode":  "NOT_FOUND",
 *   "message":    "User not found with id: 99",
 *   "path":       "uri=/api/users/99",
 *   "fieldErrors": { "email": "must not be blank" }   // only on validation errors
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    /** Convenience factory for errors without field-level detail. */
    public static ApiError of(int status, String errorCode, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, errorCode, message, path, null);
    }

    /** Factory for @Valid / @RequestBody validation failures. */
    public static ApiError ofValidation(String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(LocalDateTime.now(), 400, "VALIDATION_FAILED", message, path, fieldErrors);
    }
}
