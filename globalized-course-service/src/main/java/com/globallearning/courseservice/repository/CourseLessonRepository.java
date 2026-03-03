package com.globallearning.courseservice.repository;

import com.globallearning.courseservice.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for the course lesson manifest.
 * Used to retrieve the total lesson count for progress percentage calculation.
 */
@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, UUID> {

    long countByCourseId(UUID courseId);

    List<CourseLesson> findByCourseIdOrderBySortOrderAsc(UUID courseId);
}
