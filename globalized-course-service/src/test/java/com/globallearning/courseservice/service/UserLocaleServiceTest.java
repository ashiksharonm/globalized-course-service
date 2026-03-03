package com.globallearning.courseservice.service;

import com.globallearning.courseservice.dto.request.LocalePreferenceRequest;
import com.globallearning.courseservice.dto.response.UserLocaleResponse;
import com.globallearning.courseservice.entity.UserLocalePreference;
import com.globallearning.courseservice.exception.LocaleNotSupportedException;
import com.globallearning.courseservice.repository.UserLocalePreferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserLocaleService}.
 */
@ExtendWith(MockitoExtension.class)
class UserLocaleServiceTest {

    @Mock
    private UserLocalePreferenceRepository preferenceRepository;

    @InjectMocks
    private UserLocaleService service;

    private final String USER_ID = "user-123";

    @Nested
    @DisplayName("getLocale()")
    class GetLocaleTests {

        @Test
        @DisplayName("Returns stored preferences when they exist")
        void returnsStoredPreferences() {
            UserLocalePreference stored = UserLocalePreference.builder()
                    .userId(USER_ID).language("fr").region("CA").timezone("America/Toronto")
                    .updatedAt(OffsetDateTime.now()).build();
            when(preferenceRepository.findById(USER_ID)).thenReturn(Optional.of(stored));

            UserLocaleResponse response = service.getLocale(USER_ID);

            assertThat(response.language()).isEqualTo("fr");
            assertThat(response.region()).isEqualTo("CA");
            assertThat(response.timezone()).isEqualTo("America/Toronto");
        }

        @Test
        @DisplayName("Returns sensible defaults when no preferences stored")
        void returnsDefaultsWhenNotFound() {
            when(preferenceRepository.findById(USER_ID)).thenReturn(Optional.empty());

            UserLocaleResponse response = service.getLocale(USER_ID);

            assertThat(response.language()).isEqualTo("en");
            assertThat(response.timezone()).isEqualTo("UTC");
            assertThat(response.region()).isNull();
        }
    }

    @Nested
    @DisplayName("upsertLocale()")
    class UpsertLocaleTests {

        @Test
        @DisplayName("Creates new preference for first-time user")
        void createsNewPreference() {
            LocalePreferenceRequest request = new LocalePreferenceRequest("fr", "CA", "America/Toronto");
            when(preferenceRepository.findById(USER_ID)).thenReturn(Optional.empty());
            when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserLocaleResponse response = service.upsertLocale(USER_ID, request);

            assertThat(response.language()).isEqualTo("fr");
            assertThat(response.timezone()).isEqualTo("America/Toronto");
        }

        @Test
        @DisplayName("Updates existing preference for returning user")
        void updatesExistingPreference() {
            UserLocalePreference existing = UserLocalePreference.builder()
                    .userId(USER_ID).language("en").region(null).timezone("UTC")
                    .updatedAt(OffsetDateTime.now()).build();
            LocalePreferenceRequest request = new LocalePreferenceRequest("ja", "JP", "Asia/Tokyo");
            when(preferenceRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserLocaleResponse response = service.upsertLocale(USER_ID, request);

            assertThat(response.language()).isEqualTo("ja");
            assertThat(response.timezone()).isEqualTo("Asia/Tokyo");
        }

        @Test
        @DisplayName("Throws LocaleNotSupportedException for invalid IANA timezone")
        void throwsForInvalidTimezone() {
            LocalePreferenceRequest request = new LocalePreferenceRequest("fr", null, "Not/A/Valid/Zone");

            assertThatThrownBy(() -> service.upsertLocale(USER_ID, request))
                    .isInstanceOf(LocaleNotSupportedException.class)
                    .hasMessageContaining("Not/A/Valid/Zone");
            verify(preferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Accepts valid IANA timezone without error")
        void acceptsValidIanaTimezone() {
            LocalePreferenceRequest request = new LocalePreferenceRequest("ar", null, "Asia/Riyadh");
            when(preferenceRepository.findById(USER_ID)).thenReturn(Optional.empty());
            when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserLocaleResponse response = service.upsertLocale(USER_ID, request);
            assertThat(response.timezone()).isEqualTo("Asia/Riyadh");
        }
    }
}
