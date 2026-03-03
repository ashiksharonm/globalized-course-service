package com.globallearning.courseservice.service;

import com.globallearning.courseservice.entity.CourseTranslation;
import com.globallearning.courseservice.repository.CourseTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the best available {@link CourseTranslation} for a given BCP 47
 * locale tag
 * using a three-step fallback chain: full tag → primary language → "en" base.
 *
 * <p>
 * <b>Why this lives here, not in SQL:</b> Implementing locale fallback in SQL
 * requires
 * dialect-specific string parsing (e.g. PostgreSQL's {@code regexp_replace},
 * {@code SPLIT_PART})
 * that is not portable and breaks on H2 in tests. Doing it in Java keeps the
 * logic
 * unit-testable without any DB dependency.
 *
 * <p>
 * <b>Performance note:</b> The chain is at most 3 sequential queries. Each
 * query is a
 * PK-or-unique-index lookup. The overhead is acceptable at this scale. If
 * profiling shows
 * this is a hotspot, add a {@code @Cacheable} at the service layer.
 *
 * <p>
 * Example chains:
 * <ul>
 * <li>"fr-CA" → tries "fr-CA", then "fr", then "en"</li>
 * <li>"en-GB" → tries "en-GB", then "en" (same as base — one fewer hop)</li>
 * <li>"en" → tries "en" directly</li>
 * <li>"" → falls through to "en"</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocaleResolutionService {

    private static final String BASE_LOCALE = "en";

    private final CourseTranslationRepository translationRepository;

    /**
     * Result of locale resolution: the found translation and the locale actually
     * used.
     */
    public record ResolutionResult(CourseTranslation translation, String resolvedLocale, boolean fallbackUsed) {
    }

    /**
     * Resolves the best matching translation for the given course and locale.
     *
     * @param courseId        the course to look up
     * @param requestedLocale the BCP 47 locale requested (may be null or blank)
     * @return an {@link Optional} containing the resolution result, or empty if no
     *         translation
     *         exists at all (including the "en" base)
     */
    public Optional<ResolutionResult> resolve(UUID courseId, String requestedLocale) {
        List<String> chain = buildFallbackChain(requestedLocale);
        log.debug("Locale fallback chain for courseId={}, requested='{}': {}", courseId, requestedLocale, chain);

        for (String candidate : chain) {
            Optional<CourseTranslation> translation = translationRepository.findByCourseIdAndLocale(courseId,
                    candidate);
            if (translation.isPresent()) {
                boolean fallbackUsed = !candidate.equals(normalizeLocale(requestedLocale));
                log.debug("Resolved locale '{}' for courseId={} (fallback={})", candidate, courseId, fallbackUsed);
                return Optional.of(new ResolutionResult(translation.get(), candidate, fallbackUsed));
            }
        }

        log.warn("No translation found for courseId={} in any locale of chain: {}", courseId, chain);
        return Optional.empty();
    }

    /**
     * Builds the ordered fallback chain for a given BCP 47 locale string.
     * Deduplication preserves order and avoids checking the same locale twice
     * (e.g. for "en" the chain would be ["en", "en"] without dedup → ["en"]).
     */
    List<String> buildFallbackChain(String requestedLocale) {
        List<String> chain = new ArrayList<>();
        String normalized = normalizeLocale(requestedLocale);

        if (!normalized.isEmpty()) {
            chain.add(normalized);

            // Extract primary language subtag if the locale has a region (e.g. "fr-CA" →
            // "fr")
            Locale locale;
            try {
                locale = Locale.forLanguageTag(normalized);
            } catch (Exception e) {
                locale = Locale.ROOT;
            }
            String primaryLanguage = locale.getLanguage();
            if (!primaryLanguage.isEmpty() && !primaryLanguage.equals(normalized)) {
                chain.add(primaryLanguage);
            }
        }

        // Always end with the base "en" fallback
        if (!chain.contains(BASE_LOCALE)) {
            chain.add(BASE_LOCALE);
        }

        return chain;
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "";
        }
        return locale.trim();
    }
}
