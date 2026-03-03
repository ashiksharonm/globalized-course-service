package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.request.ProgressEventRequest;
import com.globallearning.courseservice.dto.response.ProgressResponse;
import com.globallearning.courseservice.exception.ResourceNotFoundException;
import com.globallearning.courseservice.repository.CourseLessonRepository;
import com.globallearning.courseservice.repository.CourseRepository;
import com.globallearning.courseservice.repository.LessonProgressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProgressService}.
 */
@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LessonProgressRepository progressRepository;
    @Mock
    private CourseLessonRepository lessonRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProgressService service;

    private final UUID COURSE_ID = UUID.randomUUID();
    private final String USER_ID = "user-42";

    @Nested
    @DisplayName("recordProgress()")
    class RecordProgressTests {

        @Test
        @DisplayName("Records new lesson and returns current progress")
        void recordsNewLesson() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.insertIfNotExists(USER_ID, COURSE_ID, "lesson-01")).thenReturn(1);
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(3L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(10L);
            when(messageSource.getMessage(eq("progress.message"), any(), any(Locale.class)))
                    .thenReturn("30% complete — keep going!");

            ProgressResponse response = service.recordProgress(USER_ID, COURSE_ID,
                    new ProgressEventRequest("lesson-01"), "en");

            assertThat(response.completedLessons()).isEqualTo(3);
            assertThat(response.totalLessons()).isEqualTo(10);
            assertThat(response.percentageComplete()).isEqualTo(30);
            assertThat(response.progressMessage()).contains("30%");
        }

        @Test
        @DisplayName("Idempotent replay (return 0 from insertIfNotExists) does not throw")
        void idempotentReplayDoesNotThrow() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.insertIfNotExists(USER_ID, COURSE_ID, "lesson-01")).thenReturn(0); // already exists
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(1L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(5L);
            when(messageSource.getMessage(eq("progress.message"), any(), any(Locale.class)))
                    .thenReturn("20% complete");

            ProgressResponse response = service.recordProgress(USER_ID, COURSE_ID,
                    new ProgressEventRequest("lesson-01"), "en");

            assertThat(response.percentageComplete()).isEqualTo(20);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when course does not exist")
        void throwsWhenCourseNotFound() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.recordProgress(USER_ID, COURSE_ID,
                    new ProgressEventRequest("lesson-01"), "en"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProgress()")
    class GetProgressTests {

        @Test
        @DisplayName("Returns 0% when no lessons completed")
        void returnsZeroWhenNoLessonsCompleted() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(0L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(5L);
            when(messageSource.getMessage(eq("progress.message"), any(), any(Locale.class)))
                    .thenReturn("0% complete");

            ProgressResponse response = service.getProgress(USER_ID, COURSE_ID, "en");

            assertThat(response.percentageComplete()).isEqualTo(0);
            assertThat(response.completedLessons()).isEqualTo(0);
        }

        @Test
        @DisplayName("Returns 100% when all lessons completed")
        void returnsHundredWhenAllComplete() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(5L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(5L);
            when(messageSource.getMessage(eq("progress.message"), any(), any(Locale.class)))
                    .thenReturn("100% complete");

            ProgressResponse response = service.getProgress(USER_ID, COURSE_ID, "en");

            assertThat(response.percentageComplete()).isEqualTo(100);
        }

        @Test
        @DisplayName("Returns 0% gracefully when course has no lessons")
        void returnsSafelyWhenNoLessons() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(0L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(0L);
            when(messageSource.getMessage(eq("progress.message"), any(), any(Locale.class)))
                    .thenReturn("0% complete");

            ProgressResponse response = service.getProgress(USER_ID, COURSE_ID, "en");
            assertThat(response.percentageComplete()).isEqualTo(0);
        }

        @Test
        @DisplayName("Locale is passed to MessageSource for localized message")
        void passesLocaleToMessageSource() {
            when(courseRepository.existsById(COURSE_ID)).thenReturn(true);
            when(progressRepository.countByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(3L);
            when(lessonRepository.countByCourseId(COURSE_ID)).thenReturn(10L);
            when(messageSource.getMessage(eq("progress.message"), any(), eq(Locale.FRENCH)))
                    .thenReturn("30% terminé — continuez !");

            ProgressResponse response = service.getProgress(USER_ID, COURSE_ID, "fr");

            assertThat(response.progressMessage()).contains("terminé");
            verify(messageSource).getMessage(eq("progress.message"), any(), eq(Locale.FRENCH));
        }
    }
}
