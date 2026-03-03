package com.globallearning.courseservice.repository;

import com.globallearning.courseservice.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link LessonProgress}.
 *
 * <p>
 * Idempotent insert: if a (user_id, lesson_id) row already exists,
 * the {@code INSERT ... ON CONFLICT DO NOTHING} native query silently skips it.
 * This is safer than application-level check-then-insert (TOCTOU race) and
 * avoids
 * throwing a constraint violation at the service layer.
 */
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    long countByUserIdAndCourseId(String userId, UUID courseId);

    boolean existsByUserIdAndLessonId(String userId, String lessonId);

    /**
     * Idempotent upsert: inserts a new lesson progress row.
     * If the same (user_id, lesson_id) already exists, the row is silently skipped.
     * Returns 1 if inserted, 0 if skipped.
     */
    @Modifying
    @Query(value = """
            INSERT INTO lesson_progress (id, user_id, course_id, lesson_id, completed_at)
            VALUES (gen_random_uuid(), :userId, :courseId, :lessonId, NOW())
            ON CONFLICT (user_id, lesson_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfNotExists(
            @Param("userId") String userId,
            @Param("courseId") UUID courseId,
            @Param("lessonId") String lessonId);
}
