package com.globallearning.courseservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for upserting a user's locale preferences.
 *
 * <p>
 * All fields are validated as follows:
 * <ul>
 * <li>{@code language}: required, 2-8 character BCP 47 primary language
 * tag</li>
 * <li>{@code region}: optional, exactly 2 uppercase alpha characters (ISO
 * 3166-1 alpha-2)</li>
 * <li>{@code timezone}: required, IANA timezone ID validated at the service
 * layer
 * (not via regex — IANA IDs include slashes and underscores with no fixed
 * format)</li>
 * </ul>
 */
@Schema(description = "Request to upsert user locale preferences")
public record LocalePreferenceRequest(

        @NotBlank(message = "language must not be blank") @Pattern(regexp = "^[a-zA-Z]{2,8}(-[a-zA-Z0-9]{1,8})*$", message = "language must be a valid BCP 47 language tag") @Schema(description = "BCP 47 primary language tag (e.g. 'fr', 'ar', 'zh-Hant')") String language,

        @Pattern(regexp = "^[A-Z]{2}$", message = "region must be a 2-letter ISO 3166-1 alpha-2 code") @Schema(description = "ISO 3166-1 alpha-2 region code (optional, e.g. 'CA')") String region,

        @NotBlank(message = "timezone must not be blank") @Size(max = 64, message = "timezone must be at most 64 characters") @Schema(description = "IANA timezone ID (e.g. 'America/Toronto', 'Asia/Tokyo')") String timezone) {
}
