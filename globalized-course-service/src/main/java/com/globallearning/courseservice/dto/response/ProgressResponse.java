package com.globallearning.courseservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

/**
 * Response payload for a user's progress in a specific course.
 *
 * <p>
 * {@code progressMessage} is localized at the service layer using the Spring
 * MessageSource. Example values:
 * <ul>
 * <li>en: "35% complete — keep going!"</li>
 * <li>fr: "35% terminé — continuez !"</li>
 * <li>ar: "اكتملت 35% — واصل!"</li>
 * </ul>
 *
 * <p>
 * This design keeps the frontend display-only while allowing the backend to own
 * all locale-sensitive copy.
 */
@Builder
@Schema(description = "User progress summary for a course")
public record ProgressResponse(

        @Schema(description = "Unique course identifier") UUID courseId,

        @Schema(description = "External user identifier") String userId,

        @Schema(description = "Number of lessons the user has completed") long completedLessons,

        @Schema(description = "Total number of lessons in the course") long totalLessons,

        @Schema(description = "Completion percentage (0–100)") int percentageComplete,

        @Schema(description = "Localized progress message (e.g. '35% terminé — continuez !')") String progressMessage,

        @Schema(description = "BCP 47 locale used to generate the progress message") String locale) {
}
