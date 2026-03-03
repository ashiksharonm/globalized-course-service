package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.response.CoursePreviewResponse;
import com.globallearning.courseservice.entity.Course;
import com.globallearning.courseservice.entity.CourseTranslation;
import com.globallearning.courseservice.exception.ResourceNotFoundException;
import com.globallearning.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CoursePreviewService}.
 * DB and locale resolution are mocked — only service-layer orchestration is
 * tested here.
 */
@ExtendWith(MockitoExtension.class)
class CoursePreviewServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LocaleResolutionService localeResolutionService;

    @InjectMocks
    private CoursePreviewService service;

    private final UUID COURSE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("getPreview()")
    class GetPreviewTests {

        @Test
        @DisplayName("Returns localized response when course and translation exist")
        void returnsLocalizedResponse() {
            Course course = Course.builder().id(COURSE_ID).slug("intro-ml").durationSecs(9000).build();
            CourseTranslation translation = CourseTranslation.builder()
                    .locale("fr").title("Intro ML FR").description("Desc FR").build();
            LocaleResolutionService.ResolutionResult resolutionResult = new LocaleResolutionService.ResolutionResult(
                    translation, "fr", true);

            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
            when(localeResolutionService.resolve(COURSE_ID, "fr-CA")).thenReturn(Optional.of(resolutionResult));

            CoursePreviewResponse response = service.getPreview(COURSE_ID, "fr-CA");

            assertThat(response.title()).isEqualTo("Intro ML FR");
            assertThat(response.locale()).isEqualTo("fr");
            assertThat(response.fallbackUsed()).isTrue();
            assertThat(response.durationFormatted()).isEqualTo("2h 30min");
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when course not found")
        void throwsWhenCourseNotFound() {
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPreview(COURSE_ID, "fr"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(COURSE_ID.toString());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when no translation available")
        void throwsWhenNoTranslationAvailable() {
            Course course = Course.builder().id(COURSE_ID).slug("intro-ml").durationSecs(9000).build();
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
            when(localeResolutionService.resolve(COURSE_ID, "xx")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPreview(COURSE_ID, "xx"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("formatDuration()")
    class FormatDurationTests {

        @ParameterizedTest
        @CsvSource({
                "9000,  en, '2h 30min'",
                "3600,  en, '2h'", // edge: minutes = 0
                "1800,  en, '30min'", // edge: hours = 0
                "9000,  fr, '2h 30min'", // French uses same format
                "9000,  ja, '2時間30分'", // Japanese locale-specific format
                "3600,  ja, '2時間'",
                "1800,  ja, '30分'"
        })
        @DisplayName("Formats duration correctly per locale")
        void formatsDurationPerLocale(int seconds, String locale, String expected) {
            assertThat(service.formatDuration(seconds, locale)).isEqualTo(expected);
        }
    }
}
