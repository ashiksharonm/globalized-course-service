package com.globallearning.courseservice.repository;

import com.globallearning.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Course} entities.
 * Only DB access belongs here — business logic lives in the service layer.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findBySlug(String slug);
}
