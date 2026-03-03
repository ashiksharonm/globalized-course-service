package com.globallearning.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Stores a single locale-specific version of a course's title and description.
 *
 * <p>
 * One row per (course, locale). The locale follows IETF BCP 47 (e.g. "fr-CA",
 * "fr", "en").
 * The fallback chain (fr-CA → fr → en) is resolved in
 * {@code LocaleResolutionService},
 * not here, keeping this entity simple and the DB normalized.
 */
@Entity
@Table(name = "course_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "course_id", "locale" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * IETF BCP 47 locale tag. Can be a full tag ("fr-CA") or a primary tag ("fr").
     * The fallback base ("en") serves as the guaranteed fallback row.
     */
    @Column(nullable = false, length = 16)
    private String locale;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
