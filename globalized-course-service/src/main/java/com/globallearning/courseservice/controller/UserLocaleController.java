package com.globallearning.courseservice.controller;

import com.globallearning.courseservice.dto.request.LocalePreferenceRequest;
import com.globallearning.courseservice.dto.response.UserLocaleResponse;
import com.globallearning.courseservice.service.UserLocaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user locale preferences.
 *
 * <p>
 * GET returns current preferences (or a sensible default for first-time users).
 * PUT is idempotent — repeated calls with the same body produce the same
 * result.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Locale", description = "User locale preference management")
public class UserLocaleController {

    private final UserLocaleService userLocaleService;

    @GetMapping("/{userId}/locale")
    @Operation(summary = "Get user locale preferences", description = "Returns stored locale preferences, or sensible defaults (en / UTC) if none are set.")
    @ApiResponse(responseCode = "200", description = "Locale preferences returned")
    public ResponseEntity<UserLocaleResponse> getLocale(@PathVariable String userId) {
        return ResponseEntity.ok(userLocaleService.getLocale(userId));
    }

    @PutMapping("/{userId}/locale")
    @Operation(summary = "Set user locale preferences", description = "Upserts language, region, and timezone. IANA timezone IDs are required (e.g. 'America/Toronto').")
    @ApiResponse(responseCode = "200", description = "Locale preferences updated")
    @ApiResponse(responseCode = "400", description = "Invalid locale or timezone")
    public ResponseEntity<UserLocaleResponse> upsertLocale(
            @PathVariable String userId,
            @Valid @RequestBody LocalePreferenceRequest request) {
        return ResponseEntity.ok(userLocaleService.upsertLocale(userId, request));
    }
}
