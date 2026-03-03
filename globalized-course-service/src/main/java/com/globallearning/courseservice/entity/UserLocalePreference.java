package com.globallearning.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Persists a user's preferred language, region, and timezone.
 *
 * <p>
 * One row per user (primary key is the external user ID from the identity
 * provider).
 * IANA timezone IDs are used (e.g. "America/Toronto") so conversion is
 * DST-safe.
 * UPSERTs are safe: the service uses {@code save()} which maps to INSERT ON
 * CONFLICT.
 */
@Entity
@Table(name = "user_locale_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLocalePreference {

    /**
     * External user identifier (e.g. Keycloak sub claim). Not a UUID generated
     * here;
     * it comes from the identity provider. Using VARCHAR to remain
     * provider-agnostic.
     */
    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    /**
     * BCP 47 primary language tag (e.g. "fr", "ar", "ja").
     */
    @Column(nullable = false, length = 16)
    private String language;

    /**
     * ISO 3166-1 alpha-2 region code (e.g. "CA", "JP"). Nullable — region is
     * optional.
     */
    @Column(length = 16)
    private String region;

    /**
     * IANA timezone ID (e.g. "America/Toronto", "Asia/Tokyo").
     * Stored here so all timestamp displays respect the user's clock, not the
     * server's.
     */
    @Column(nullable = false, length = 64)
    private String timezone;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = OffsetDateTime.now();
    }
}
