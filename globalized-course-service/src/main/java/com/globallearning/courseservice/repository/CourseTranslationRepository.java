package com.globallearning.courseservice.repository;

import com.globallearning.courseservice.entity.CourseTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link CourseTranslation}.
 *
 * <p>
 * The key query here is {@code findByCourseIdAndLocale}, used by
 * {@code LocaleResolutionService} to walk the BCP 47 fallback chain
 * one locale at a time (fr-CA → fr → en).
 *
 * <p>
 * We do NOT attempt a single DB query for the fallback chain because:
 * (a) it requires complex SQL with language tag parsing not portable across
 * DBs,
 * (b) the fallback chain is at most 3 hops — the overhead is negligible, and
 * (c) keeping the query simple means it stays easy to test.
 */
@Repository
public interface CourseTranslationRepository extends JpaRepository<CourseTranslation, UUID> {

    Optional<CourseTranslation> findByCourseIdAndLocale(UUID courseId, String locale);

    List<CourseTranslation> findByCourseId(UUID courseId);
}
