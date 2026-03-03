package com.globallearning.courseservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * Response payload carrying a user's stored locale preferences.
 * The {@code updatedAt} timestamp is in UTC ISO-8601 format.
 */
@Builder
@Schema(description = "User locale preferences")
public record UserLocaleResponse(

        @Schema(description = "External user identifier") String userId,

        @Schema(description = "BCP 47 primary language tag (e.g. 'fr', 'ar')") String language,

        @Schema(description = "ISO 3166-1 alpha-2 region code (e.g. 'CA'). May be null.") String region,

        @Schema(description = "IANA timezone ID (e.g. 'America/Toronto')") String timezone,

        @Schema(description = "UTC timestamp of the last preference update") OffsetDateTime updatedAt) {
}
