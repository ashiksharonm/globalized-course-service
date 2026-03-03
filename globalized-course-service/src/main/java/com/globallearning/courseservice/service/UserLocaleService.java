package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.request.LocalePreferenceRequest;
import com.globallearning.courseservice.dto.response.UserLocaleResponse;
import com.globallearning.courseservice.entity.UserLocalePreference;
import com.globallearning.courseservice.exception.LocaleNotSupportedException;
import com.globallearning.courseservice.repository.UserLocalePreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

/**
 * Service for managing user locale preferences.
 *
 * <p>
 * Upsert semantics: calling {@link #upsertLocale} for an existing user
 * overwrites
 * their preference; for a new user it creates a new row. JPA's {@code save()}
 * handles both.
 *
 * <p>
 * IANA timezone validation is done in this layer (not the controller) because
 * it requires trying to construct a {@link ZoneId} from the string — this is
 * business logic,
 * not HTTP input parsing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocaleService {

    private final UserLocalePreferenceRepository preferenceRepository;

    /**
     * Fetches locale preferences for the given user.
     * If no preference has been set, returns a sensible default (en / UTC).
     */
    @Transactional(readOnly = true)
    public UserLocaleResponse getLocale(String userId) {
        return preferenceRepository.findById(userId)
                .map(this::toResponse)
                .orElseGet(() -> defaultResponse(userId));
    }

    /**
     * Upserts locale preferences for the given user.
     *
     * @throws LocaleNotSupportedException if the timezone ID is not a valid IANA
     *                                     zone
     */
    @Transactional
    public UserLocaleResponse upsertLocale(String userId, LocalePreferenceRequest request) {
        validateTimezone(request.timezone());

        UserLocalePreference preference = preferenceRepository.findById(userId)
                .orElseGet(() -> UserLocalePreference.builder().userId(userId).build());

        preference.setLanguage(request.language());
        preference.setRegion(request.region());
        preference.setTimezone(request.timezone());

        UserLocalePreference saved = preferenceRepository.save(preference);
        log.info("Locale preference updated for userId={}: {}/{}/{}", userId, saved.getLanguage(), saved.getRegion(),
                saved.getTimezone());
        return toResponse(saved);
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new LocaleNotSupportedException("Invalid IANA timezone: '" + timezone + "'");
        }
    }

    private UserLocaleResponse toResponse(UserLocalePreference pref) {
        return UserLocaleResponse.builder()
                .userId(pref.getUserId())
                .language(pref.getLanguage())
                .region(pref.getRegion())
                .timezone(pref.getTimezone())
                .updatedAt(pref.getUpdatedAt())
                .build();
    }

    private UserLocaleResponse defaultResponse(String userId) {
        return UserLocaleResponse.builder()
                .userId(userId)
                .language("en")
                .region(null)
                .timezone("UTC")
                .updatedAt(null)
                .build();
    }
}
