package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.response.CoursePreviewResponse;
import com.globallearning.courseservice.entity.Course;
import com.globallearning.courseservice.exception.ResourceNotFoundException;
import com.globallearning.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for serving locale-aware course preview data.
 *
 * <p>
 * <b>Responsibilities:</b>
 * <ol>
 * <li>Load the course entity by ID</li>
 * <li>Delegate locale resolution (with BCP 47 fallback) to
 * {@link LocaleResolutionService}</li>
 * <li>Format the duration for the resolved locale</li>
 * <li>Map to {@link CoursePreviewResponse}</li>
 * </ol>
 *
 * <p>
 * <b>What this class does NOT do:</b> DB access, locale string parsing.
 * Those are owned by the repository and {@link LocaleResolutionService}
 * respectively,
 * keeping this class independently unit-testable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePreviewService {

    private final CourseRepository courseRepository;
    private final LocaleResolutionService localeResolutionService;

    /**
     * Returns a localized preview for the given course.
     *
     * @param courseId        UUID of the course
     * @param requestedLocale BCP 47 locale from the Accept-Language header (may be
     *                        null)
     * @return localized course preview
     * @throws ResourceNotFoundException if the course does not exist
     * @throws ResourceNotFoundException if no translation exists (including en
     *                                   fallback)
     */
    @Transactional(readOnly = true)
    public CoursePreviewResponse getPreview(UUID courseId, String requestedLocale) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        LocaleResolutionService.ResolutionResult result = localeResolutionService
                .resolve(courseId, requestedLocale)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No translation available for course " + courseId + " in any supported locale"));

        String durationFormatted = formatDuration(course.getDurationSecs(), result.resolvedLocale());

        return CoursePreviewResponse.builder()
                .courseId(course.getId())
                .title(result.translation().getTitle())
                .description(result.translation().getDescription())
                .durationFormatted(durationFormatted)
                .locale(result.resolvedLocale())
                .fallbackUsed(result.fallbackUsed())
                .build();
    }

    /**
     * Formats a duration in seconds to a human-readable string for the given
     * locale.
     *
     * <p>
     * Currently produces "Xh Ymin" regardless of locale (e.g. "2h 30min").
     * This is intentional: hour/minute abbreviations vary across locales but are
     * universally understood. A future iteration can look up locale-specific
     * abbreviations from the i18n message bundle (e.g. "2h 30 min" in fr vs
     * "2時間30分" in ja).
     *
     * <p>
     * Design note: this method is package-private to allow direct unit testing.
     */
    String formatDuration(int totalSeconds, String locale) {
        Duration duration = Duration.ofSeconds(totalSeconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        // For Japanese, format as X時間Ymin — demonstrating locale-specific extension
        // point
        Locale javaLocale = Locale.forLanguageTag(locale);
        if ("ja".equals(javaLocale.getLanguage())) {
            if (hours > 0 && minutes > 0)
                return hours + "時間" + minutes + "分";
            if (hours > 0)
                return hours + "時間";
            return minutes + "分";
        }

        if (hours > 0 && minutes > 0)
            return hours + "h " + minutes + "min";
        if (hours > 0)
            return hours + "h";
        return minutes + "min";
    }
}
