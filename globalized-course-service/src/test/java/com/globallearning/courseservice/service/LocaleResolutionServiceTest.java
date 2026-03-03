package com.globallearning.courseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.globallearning.courseservice.entity.CourseTranslation;
import com.globallearning.courseservice.repository.CourseTranslationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LocaleResolutionService}.
 *
 * <p>
 * Tests are isolated from the DB by mocking
 * {@link CourseTranslationRepository}.
 * This validates the fallback chain logic — the most business-critical
 * component of the
 * globalization layer — without requiring any Spring context.
 */
@ExtendWith(MockitoExtension.class)
class LocaleResolutionServiceTest {

    @Mock
    private CourseTranslationRepository translationRepository;

    @InjectMocks
    private LocaleResolutionService service;

    private final UUID COURSE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("buildFallbackChain()")
    class FallbackChainTests {

        @Test
        @DisplayName("Full BCP-47 tag produces 3-element chain [full, lang, en]")
        void fullTagProducesThreeHopChain() {
            List<String> chain = service.buildFallbackChain("fr-CA");
            assertThat(chain).containsExactly("fr-CA", "fr", "en");
        }

        @Test
        @DisplayName("Primary-only tag produces 2-element chain [lang, en]")
        void primaryTagProducesTwoHopChain() {
            List<String> chain = service.buildFallbackChain("fr");
            assertThat(chain).containsExactly("fr", "en");
        }

        @Test
        @DisplayName("'en' input deduplicates to single element [en]")
        void englishInputDeduplicates() {
            List<String> chain = service.buildFallbackChain("en");
            assertThat(chain).containsExactly("en");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Null or blank locale falls through to [en] only")
        void nullOrBlankFallsToEnglish(String locale) {
            List<String> chain = service.buildFallbackChain(locale);
            assertThat(chain).containsExactly("en");
        }

        @Test
        @DisplayName("Arabic locale chain: ar → en")
        void arabicChain() {
            List<String> chain = service.buildFallbackChain("ar");
            assertThat(chain).containsExactly("ar", "en");
        }

        @Test
        @DisplayName("en-GB deduplicates correctly to [en-GB, en]")
        void enGbDeduplicatesCorrectly() {
            List<String> chain = service.buildFallbackChain("en-GB");
            assertThat(chain).containsExactly("en-GB", "en");
        }
    }

    @Nested
    @DisplayName("resolve()")
    class ResolveTests {

        @Test
        @DisplayName("Exact locale match — no fallback")
        void exactLocaleMatch() {
            CourseTranslation frCa = buildTranslation("fr-CA");
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "fr-CA"))
                    .thenReturn(Optional.of(frCa));

            Optional<LocaleResolutionService.ResolutionResult> result = service.resolve(COURSE_ID, "fr-CA");

            assertThat(result).isPresent();
            assertThat(result.get().resolvedLocale()).isEqualTo("fr-CA");
            assertThat(result.get().fallbackUsed()).isFalse();
        }

        @Test
        @DisplayName("fr-CA misses, falls back to fr")
        void fallsBackToLanguage() {
            CourseTranslation frTranslation = buildTranslation("fr");
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "fr-CA"))
                    .thenReturn(Optional.empty());
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "fr"))
                    .thenReturn(Optional.of(frTranslation));

            Optional<LocaleResolutionService.ResolutionResult> result = service.resolve(COURSE_ID, "fr-CA");

            assertThat(result).isPresent();
            assertThat(result.get().resolvedLocale()).isEqualTo("fr");
            assertThat(result.get().fallbackUsed()).isTrue();
        }

        @Test
        @DisplayName("fr-CA and fr miss, falls back to en")
        void fallsBackToEnglish() {
            CourseTranslation enTranslation = buildTranslation("en");
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "fr-CA"))
                    .thenReturn(Optional.empty());
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "fr"))
                    .thenReturn(Optional.empty());
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "en"))
                    .thenReturn(Optional.of(enTranslation));

            Optional<LocaleResolutionService.ResolutionResult> result = service.resolve(COURSE_ID, "fr-CA");

            assertThat(result).isPresent();
            assertThat(result.get().resolvedLocale()).isEqualTo("en");
            assertThat(result.get().fallbackUsed()).isTrue();
        }

        @Test
        @DisplayName("No translations exist at all — returns empty")
        void noTranslationsExist() {
            when(translationRepository.findByCourseIdAndLocale(any(), any()))
                    .thenReturn(Optional.empty());

            Optional<LocaleResolutionService.ResolutionResult> result = service.resolve(COURSE_ID, "ja");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Null locale requested — falls back to en")
        void nullLocaleRequestedFallsToEn() {
            CourseTranslation enTranslation = buildTranslation("en");
            when(translationRepository.findByCourseIdAndLocale(COURSE_ID, "en"))
                    .thenReturn(Optional.of(enTranslation));

            Optional<LocaleResolutionService.ResolutionResult> result = service.resolve(COURSE_ID, null);

            assertThat(result).isPresent();
            assertThat(result.get().resolvedLocale()).isEqualTo("en");
        }
    }

    private CourseTranslation buildTranslation(String locale) {
        return CourseTranslation.builder()
                .id(UUID.randomUUID())
                .locale(locale)
                .title("Title in " + locale)
                .description("Description in " + locale)
                .build();
    }
}
