package com.globallearning.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Records a single lesson completion event for a user.
 *
 * <p>
 * The UNIQUE constraint on (user_id, lesson_id) is the idempotency mechanism.
 * Replaying the same event (e.g. from a network retry) is handled with
 * {@code INSERT ... ON CONFLICT DO NOTHING} at the repository level, meaning
 * duplicate events are silently dropped — no exception, no double-counting.
 *
 * <p>
 * All timestamps are stored in UTC (OffsetDateTime with Z offset).
 * Conversion to the user's local timezone happens at the DTO/response layer.
 */
@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(name = "uq_lesson_progress_user_lesson", columnNames = {
        "user_id", "lesson_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "lesson_id", nullable = false, length = 128)
    private String lessonId;

    /**
     * UTC timestamp of when the lesson was first completed.
     * Not updated on replay — idempotent by design.
     */
    @Column(name = "completed_at", nullable = false, updatable = false)
    private OffsetDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.completedAt = OffsetDateTime.now();
    }
}
