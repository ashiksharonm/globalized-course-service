package com.globallearning.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Globalized Course Service.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Locale-aware course preview delivery (with BCP 47 fallback chain)</li>
 *   <li>User locale preference persistence</li>
 *   <li>Idempotent lesson progress tracking</li>
 * </ul>
 *
 * <p>All timestamps are stored in UTC. Locale resolution happens at the service layer,
 * not in SQL, to keep DB normalized and logic unit-testable.
 */
@SpringBootApplication
public class CourseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}
