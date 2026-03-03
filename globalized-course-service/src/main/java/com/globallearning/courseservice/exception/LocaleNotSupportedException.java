package com.globallearning.courseservice.exception;

/**
 * Thrown when a locale parameter (e.g. timezone) is invalid.
 * Maps to HTTP 400 via {@link GlobalExceptionHandler}.
 */
public class LocaleNotSupportedException extends RuntimeException {
    public LocaleNotSupportedException(String message) {
        super(message);
    }
}
