package com.globallearning.courseservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for recording a lesson completion event.
 * Idempotent: submitting the same lessonId multiple times is safe.
 */
@Schema(description = "Request to record a lesson completion")
public record ProgressEventRequest(

        @NotBlank(message = "lessonId must not be blank") @Size(max = 128, message = "lessonId must be at most 128 characters") @Schema(description = "External lesson identifier from the LMS") String lessonId) {
}
