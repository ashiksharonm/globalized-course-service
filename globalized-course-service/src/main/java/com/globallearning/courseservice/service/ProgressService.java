package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.request.ProgressEventRequest;
import com.globallearning.courseservice.dto.response.ProgressResponse;
import com.globallearning.courseservice.exception.ResourceNotFoundException;
import com.globallearning.courseservice.repository.CourseLessonRepository;
import com.globallearning.courseservice.repository.CourseRepository;
import com.globallearning.courseservice.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Service for tracking and querying lesson progress.
 *
 * <p>
 * <b>Idempotency:</b> {@link #recordProgress} uses a native
 * {@code INSERT ... ON CONFLICT DO NOTHING} at the repository layer.
 * This class is agnostic to whether the record was newly inserted or already
 * existed —
 * it always returns the current progress state.
 *
 * <p>
 * <b>Progress message localization:</b> The {@link MessageSource} resolves
 * the "progress.message" key from the appropriate
 * {@code messages_XX.properties} bundle.
 * This keeps locale-specific copy in properties files, not in Java code.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final LessonProgressRepository progressRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final MessageSource messageSource;

    /**
     * Records a lesson completion event (idempotent).
     * Returns the updated progress summary after the event is processed.
     *
     * @param userId   external user identifier
     * @param courseId course UUID
     * @param request  contains the lessonId
     * @param locale   BCP 47 locale for the progress message
     * @throws ResourceNotFoundException if the course does not exist
     */
    @Transactional
    public ProgressResponse recordProgress(String userId, UUID courseId, ProgressEventRequest request, String locale) {
        ensureCourseExists(courseId);

        int affected = progressRepository.insertIfNotExists(userId, courseId, request.lessonId());
        if (affected == 0) {
            log.debug("Idempotent replay: progress for userId={}, lessonId={} already recorded.", userId,
                    request.lessonId());
        }

        return buildProgressResponse(userId, courseId, locale);
    }

    /**
     * Fetches the current progress summary for a user in a course.
     *
     * @param userId   external user identifier
     * @param courseId course UUID
     * @param locale   BCP 47 locale for the progress message
     * @throws ResourceNotFoundException if the course does not exist
     */
    @Transactional(readOnly = true)
    public ProgressResponse getProgress(String userId, UUID courseId, String locale) {
        ensureCourseExists(courseId);
        return buildProgressResponse(userId, courseId, locale);
    }

    private ProgressResponse buildProgressResponse(String userId, UUID courseId, String locale) {
        long completed = progressRepository.countByUserIdAndCourseId(userId, courseId);
        long total = lessonRepository.countByCourseId(courseId);

        int percentage = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);

        Locale javaLocale = resolveJavaLocale(locale);
        String message = messageSource.getMessage(
                "progress.message",
                new Object[] { percentage },
                javaLocale);

        return ProgressResponse.builder()
                .courseId(courseId)
                .userId(userId)
                .completedLessons(completed)
                .totalLessons(total)
                .percentageComplete(percentage)
                .progressMessage(message)
                .locale(locale != null ? locale : "en")
                .build();
    }

    private void ensureCourseExists(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
    }

    /**
     * Converts a BCP 47 locale string to a Java {@link Locale}.
     * Falls back to {@link Locale#ENGLISH} if the string is null, blank, or
     * unparseable.
     */
    private Locale resolveJavaLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return Locale.ENGLISH;
        }
        try {
            Locale parsed = Locale.forLanguageTag(locale);
            // Locale.forLanguageTag returns Locale.ROOT for invalid tags — fall back to
            // English
            return parsed.getLanguage().isEmpty() ? Locale.ENGLISH : parsed;
        } catch (Exception e) {
            return Locale.ENGLISH;
        }
    }
}
