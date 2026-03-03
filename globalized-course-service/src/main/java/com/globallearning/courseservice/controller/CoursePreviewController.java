package com.globallearning.courseservice.controller;

import com.globallearning.courseservice.dto.response.CoursePreviewResponse;
import com.globallearning.courseservice.service.CoursePreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for course preview operations.
 *
 * <p>
 * Locale is read from the standard HTTP {@code Accept-Language} header.
 * This follows RFC 7231 §5.3.5 and allows CDN-level vary-caching by locale.
 * We do NOT use a custom header to avoid diverging from web standards.
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Preview", description = "Locale-aware course preview endpoints")
public class CoursePreviewController {

    private final CoursePreviewService coursePreviewService;

    @GetMapping("/{courseId}/preview")
    @Operation(summary = "Get localized course preview", description = "Returns a course title, description, and duration formatted for the best-matching locale. "
            +
            "Uses BCP 47 fallback chain: fr-CA → fr → en.")
    @ApiResponse(responseCode = "200", description = "Course preview returned successfully")
    @ApiResponse(responseCode = "404", description = "Course not found, or no translation available")
    public ResponseEntity<CoursePreviewResponse> getCoursePreview(
            @PathVariable UUID courseId,
            @RequestHeader(value = "Accept-Language", required = false) @Parameter(description = "BCP 47 locale tag (e.g. fr-CA, ar, ja). Defaults to 'en' if omitted.") String acceptLanguage) {
        CoursePreviewResponse preview = coursePreviewService.getPreview(courseId, acceptLanguage);
        return ResponseEntity.ok(preview);
    }
}
