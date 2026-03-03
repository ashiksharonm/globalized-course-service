-- V1__init_schema.sql
-- Initial schema for Globalized Course Preview & Progress Service
-- All timestamps use TIMESTAMPTZ (UTC). Locale codes follow IETF BCP 47.

-- Enable UUID generation (PostgreSQL 13+ has gen_random_uuid() built-in)

CREATE TABLE courses (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    slug          VARCHAR(128) NOT NULL UNIQUE,
    duration_secs INTEGER      NOT NULL CHECK (duration_secs > 0),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE courses IS 'Language-agnostic course metadata. Locale-specific content lives in course_translations.';
COMMENT ON COLUMN courses.duration_secs IS 'Total duration in seconds. Formatted to human-readable at the service layer per resolved locale.';

-- One row per (course, locale). Locale follows IETF BCP 47 (e.g. fr-CA, fr, en).
-- Fallback chain (fr-CA -> fr -> en) is resolved in LocaleResolutionService.
CREATE TABLE course_translations (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id    UUID         NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    locale       VARCHAR(16)  NOT NULL,
    title        VARCHAR(512) NOT NULL,
    description  TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (course_id, locale)
);
CREATE INDEX idx_course_translations_locale ON course_translations(locale);
COMMENT ON TABLE course_translations IS 'Locale-specific title and description per course. Supports BCP 47 tags.';

-- User locale preferences. PK is the external identity provider user ID.
CREATE TABLE user_locale_preferences (
    user_id    VARCHAR(128)  PRIMARY KEY,
    language   VARCHAR(16)   NOT NULL DEFAULT 'en',
    region     VARCHAR(16),
    timezone   VARCHAR(64)   NOT NULL DEFAULT 'UTC',
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN user_locale_preferences.timezone IS 'IANA timezone ID (e.g. America/Toronto). Validated at application layer.';
COMMENT ON COLUMN user_locale_preferences.region IS 'ISO 3166-1 alpha-2 region code. Nullable — region is optional.';

-- Ground-truth lesson manifest per course.
-- Progress percentage denominator comes from here, not from counting lesson_progress rows.
CREATE TABLE course_lessons (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id  UUID         NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id  VARCHAR(128) NOT NULL,
    sort_order INTEGER      NOT NULL,
    UNIQUE (course_id, lesson_id)
);
COMMENT ON TABLE course_lessons IS 'Authoritative lesson manifest per course. Used as denominator for progress percentage.';

-- Lesson completion events.
-- Idempotent: UNIQUE(user_id, lesson_id) prevents double-counting on replay.
CREATE TABLE lesson_progress (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       VARCHAR(128) NOT NULL,
    course_id     UUID         NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id     VARCHAR(128) NOT NULL,
    completed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_lesson_progress_user_lesson UNIQUE (user_id, lesson_id)
);
CREATE INDEX idx_lesson_progress_user_course ON lesson_progress(user_id, course_id);
COMMENT ON TABLE lesson_progress IS 'Records lesson completion events. Idempotent via UNIQUE(user_id, lesson_id).';

-- Seed data for development / smoke testing
INSERT INTO courses (id, slug, duration_secs) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'intro-to-ml', 9000),
    ('b1ffcd00-0d1c-5fg9-ca7e-7cc0ce491b22', 'advanced-java', 18000);

INSERT INTO course_translations (course_id, locale, title, description) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'en', 'Introduction to Machine Learning',
     'Learn the foundations of ML including supervised and unsupervised learning.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'fr', 'Introduction au Machine Learning',
     'Apprenez les bases du ML, incluant l''apprentissage supervisé et non supervisé.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ar', 'مقدمة إلى التعلم الآلي',
     'تعلّم أساسيات التعلم الآلي بما في ذلك التعلم الخاضع للإشراف وغير الخاضع له.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ja', '機械学習入門',
     '教師あり学習と教師なし学習を含む機械学習の基礎を学ぶ。'),
    ('b1ffcd00-0d1c-5fg9-ca7e-7cc0ce491b22', 'en', 'Advanced Java',
     'Deep dive into Java internals, concurrency, and performance tuning.');

INSERT INTO course_lessons (course_id, lesson_id, sort_order) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'lesson-ml-01', 1),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'lesson-ml-02', 2),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'lesson-ml-03', 3),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'lesson-ml-04', 4),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'lesson-ml-05', 5);
