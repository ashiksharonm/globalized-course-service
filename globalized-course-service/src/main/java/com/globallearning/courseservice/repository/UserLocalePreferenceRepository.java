package com.globallearning.courseservice.repository;

import com.globallearning.courseservice.entity.UserLocalePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link UserLocalePreference}.
 * JPA's {@code save()} handles both INSERT (new user) and UPDATE (existing
 * user)
 * because the PK ({@code userId}) is assigned externally — Hibernate detects
 * whether the entity is new by checking if it's managed. For true upsert safety
 * on concurrent requests, the DB UNIQUE constraint on PK covers us.
 */
@Repository
public interface UserLocalePreferenceRepository extends JpaRepository<UserLocalePreference, String> {
    // Inherits findById(userId), save(), existsById() — sufficient for our use
    // cases.
}
