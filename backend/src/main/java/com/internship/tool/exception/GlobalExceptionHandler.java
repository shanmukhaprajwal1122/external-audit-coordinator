package com.internship.tool.exception;

import com.internship.tool.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralised exception handler — every error returns the same {@link ApiError} JSON shape:
 * <pre>
 * {
 *   "timestamp": "...",
 *   "status":    404,
 *   "errorCode": "NOT_FOUND",
 *   "message":   "...",
 *   "path":      "uri=/api/...",
 *   "fieldErrors": { ... }   // only present on validation errors
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 Not Found ───────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    // ─── 400 Bad Request — custom domain validation ───────────────────────────

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex, HttpServletRequest req) {
        log.warn("Validation error: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req);
    }

    // ─── 400 Bad Request — @Valid / @RequestBody bean validation ─────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ApiError error = ApiError.ofValidation(
                "Request body validation failed — see fieldErrors for details",
                req.getRequestURI(),
                fieldErrors
        );
        log.warn("Bean validation failed on {}: {}", req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ─── 400 Bad Request — malformed JSON body ────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Malformed JSON body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Request body is missing or contains invalid JSON.", req);
    }

    // ─── 400 Bad Request — missing @RequestParam ─────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String message = String.format("Required request parameter '%s' is missing.", ex.getParameterName());
        log.warn("Missing request param: {}", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, req);
    }

    // ─── 400 Bad Request — wrong type for @PathVariable / @RequestParam ──────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = String.format(
                "Parameter '%s' must be of type '%s'.",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        log.warn("Type mismatch on '{}': {}", ex.getName(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, req);
    }

    // ─── 409 Conflict ────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req);
    }

    // ─── 403 Forbidden ───────────────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest req) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req);
    }

    /** Spring Security — access denied (JWT present but insufficient role). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied to {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You do not have permission to access this resource.", req);
    }

    // ─── 401 Unauthorised ────────────────────────────────────────────────────

    /** Spring Security — missing or invalid JWT token. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        log.warn("Authentication failure on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required to access this resource.", req);
    }

    // ─── 500 Internal Server Error ───────────────────────────────────────────

    /**
     * Catch-all for any unhandled exception.
     * The raw exception message is NOT exposed to the client (security best practice).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on [{}] {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.", req);
    }

    // ─── Private helper ──────────────────────────────────────────────────────

    private ResponseEntity<ApiError> build(HttpStatus status, String errorCode, String message, HttpServletRequest req) {
        ApiError error = ApiError.of(status.value(), errorCode, message, req.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
