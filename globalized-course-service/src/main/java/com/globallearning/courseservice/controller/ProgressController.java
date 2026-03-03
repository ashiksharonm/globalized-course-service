package com.globallearning.courseservice.controller;

import com.globallearning.courseservice.dto.request.ProgressEventRequest;
import com.globallearning.courseservice.dto.response.ProgressResponse;
import com.globallearning.courseservice.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for lesson progress tracking.
 *
 * <p>
 * POST /progress is idempotent: submitting the same lessonId twice returns 200
 * both times
 * with consistent progress data. This is safe for network retries without any
 * additional
 * client-side deduplication.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Lesson progress tracking")
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/{userId}/courses/{courseId}/progress")
    @Operation(summary = "Record a lesson completion (idempotent)", description = "Records a lesson as completed. Safe to call multiple times with the same lessonId.")
    @ApiResponse(responseCode = "200", description = "Progress recorded and summary returned")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<ProgressResponse> recordProgress(
            @PathVariable String userId,
            @PathVariable UUID courseId,
            @Valid @RequestBody ProgressEventRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(progressService.recordProgress(userId, courseId, request, acceptLanguage));
    }

    @GetMapping("/{userId}/courses/{courseId}/progress")
    @Operation(summary = "Get progress summary", description = "Returns percentage complete, lesson counts, and a localized progress message.")
    @ApiResponse(responseCode = "200", description = "Progress summary returned")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<ProgressResponse> getProgress(
            @PathVariable String userId,
            @PathVariable UUID courseId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(progressService.getProgress(userId, courseId, acceptLanguage));
    }
}
