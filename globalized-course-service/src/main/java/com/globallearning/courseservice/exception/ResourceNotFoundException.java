package com.globallearning.courseservice.exception;

/**
 * Thrown when a requested resource (course, translation) cannot be found.
 * Maps to HTTP 404 via {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
