package com.internship.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform error response body returned by GlobalExceptionHandler for every error.
 */
@Schema(description = "Standardised API Error Response DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        @Schema(description = "Timestamp when the error occurred", example = "2026-04-30T10:00:00")
        LocalDateTime timestamp,
        @Schema(description = "HTTP Status Code", example = "404")
        int status,
        @Schema(description = "Error Code", example = "NOT_FOUND")
        String errorCode,
        @Schema(description = "Human-readable error message", example = "User not found with id: 99")
        String message,
        @Schema(description = "Path of the requested resource", example = "uri=/api/users/99")
        String path,
        @Schema(description = "Map of field-specific validation errors", example = "{\"email\": \"must not be blank\"}")
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
