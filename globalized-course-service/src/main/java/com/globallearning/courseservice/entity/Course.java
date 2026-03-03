package com.globallearning.courseservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a course in its language-agnostic form.
 * Locale-specific data (title, description) lives in {@link CourseTranslation}.
 *
 * <p>
 * Duration is stored in seconds to avoid locale-specific formatting in the DB.
 * Formatting to "2h 30min" or "2 heures 30 minutes" happens at the service
 * layer.
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * URL-safe identifier for use in paths (e.g. "intro-to-ml").
     * Unique across all courses.
     */
    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    /**
     * Total duration in seconds. Avoids locale-specific time formatting in the DB.
     * Service layer formats this using the resolved locale.
     */
    @Column(name = "duration_secs", nullable = false)
    private int durationSecs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseLesson> lessons = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
