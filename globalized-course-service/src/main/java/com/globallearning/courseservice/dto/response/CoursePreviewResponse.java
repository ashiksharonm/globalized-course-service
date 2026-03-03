package com.globallearning.courseservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

/**
 * Response payload for a localized course preview.
 *
 * <p>
 * Duration is pre-formatted for the resolved locale (e.g. "2h 30min" for en,
 * "2h 30min" for fr). This keeps the frontend display-only — no formatting
 * logic needed client-side.
 *
 * <p>
 * {@code fallbackUsed} signals to the caller (and to monitoring) that the
 * requested locale
 * had no translation and a fallback was used. Useful for identifying content
 * gaps.
 */
@Builder
@Schema(description = "Localized course preview data")
public record CoursePreviewResponse(

        @Schema(description = "Unique course identifier") UUID courseId,

        @Schema(description = "Localized course title") String title,

        @Schema(description = "Localized course description") String description,

        @Schema(description = "Human-readable duration in the resolved locale (e.g. '2h 30min')") String durationFormatted,

        @Schema(description = "The BCP 47 locale tag actually used to resolve this translation") String locale,

        @Schema(description = "True if the requested locale had no translation and a fallback was applied") boolean fallbackUsed) {
}
