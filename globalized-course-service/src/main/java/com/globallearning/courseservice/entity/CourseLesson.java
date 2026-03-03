package com.globallearning.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Ground-truth lesson manifest for a course.
 *
 * <p>
 * The total lesson count for progress % calculation is derived from this table,
 * NOT from counting lesson_progress rows. This decouples "what exists" from
 * "what was done"
 * and avoids wrong percentages when a learner skips lessons.
 */
@Entity
@Table(name = "course_lessons", uniqueConstraints = @UniqueConstraint(columnNames = { "course_id", "lesson_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * External lesson identifier (e.g. LMS lesson ID). Opaque string.
     */
    @Column(name = "lesson_id", nullable = false, length = 128)
    private String lessonId;

    /**
     * 1-based ordering of lessons within the course.
     */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
