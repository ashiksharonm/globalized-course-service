# Globalized Course Preview & Progress Service

> **Internal Engineering Document** — Globalization Platform Team
> Version 1.0.0 | March 2026

---

## Overview

This module provides locale-aware course preview delivery and lesson progress tracking for the global learning platform. It is built to serve learners in **any language, timezone, and cultural context** without requiring per-locale rewrites.

```
globalized-course-ui/     React + TypeScript + i18next frontend
globalized-course-service/ Spring Boot + JPA + PostgreSQL backend
docker-compose.yml         Full stack local dev
.github/workflows/ci.yml  GitHub Actions CI pipeline
```

---

## Quick Start

```bash
# Prerequisites: Docker 24+, Docker Compose 2+

git clone <repo-url>
cd globalized-learning-platform-module

# Start the full stack (PostgreSQL + Backend + Frontend)
docker-compose up --build

# Frontend: http://localhost:3000
# Backend Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
# Backend Health: http://localhost:8080/api/v1/actuator/health
```

**Without Docker (local dev):**

```bash
# Backend
cd globalized-course-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# Requires a running PostgreSQL on localhost:5432 (see application.yml for credentials)

# Frontend
cd globalized-course-ui
npm install
npm run dev        # Starts Vite on http://localhost:5173
```

---

## REST API

Base URL: `http://localhost:8080/api/v1`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/courses/{courseId}/preview` | Localized course preview (BCP 47 fallback) |
| `GET` | `/users/{userId}/locale` | Fetch stored locale preferences |
| `PUT` | `/users/{userId}/locale` | Upsert locale preferences |
| `POST` | `/users/{userId}/courses/{courseId}/progress` | Record lesson completion (idempotent) |
| `GET` | `/users/{userId}/courses/{courseId}/progress` | Get progress summary |

**Locale is conveyed via the `Accept-Language` header on every request** (per RFC 7231 §5.3.5).

Full OpenAPI spec: `http://localhost:8080/api/v1/api-docs`

---

## Architecture

### Backend Layer Responsibilities

```
Controller  →  Input validation + HTTP contract only
Service     →  Business logic (locale fallback, progress %, message localization)
Repository  →  DB access only. No logic.
Entity      →  JPA mapping only. No business methods.
DTO         →  API contract. Separate from entity. Always explicit fields.
```

### Locale Resolution (BCP 47 Fallback Chain)

The `LocaleResolutionService` implements a three-step fallback:

```
fr-CA → fr → en
ar    → en      (primary + english)
en    → en      (deduplicates)
null  → en      (safe default)
```

**Why in Java, not SQL:** SQL locale string parsing requires dialect-specific functions that break on H2 in tests. The Java approach is unit-testable in <10ms without any DB.

### Frontend Locale Flow

```
User selects "Français"
    → useLocale.changeLocale("fr")
        → i18n.changeLanguage("fr")       (lazy-loads /locales/fr/translation.json)
        → document.documentElement.dir = "ltr"
        → document.documentElement.lang = "fr"
        → updateUserLocale(userId, { language: "fr", timezone: "Europe/Paris" })
    → useCoursePreview re-fetches with Accept-Language: fr
    → API returns French title + "1h 30min" duration
    → CoursePreviewCard re-renders — no page reload
```

---

## Key Design Decisions

### 1. Locale fallback in the application layer (not DB)
Keeps DB normalized (one row per locale) and keeps logic unit-testable without any DB round-trips.

### 2. UTC-only timestamp storage
`TIMESTAMPTZ` stored UTC always. Timezone conversion to user's IANA zone happens at the DTO layer. This is the only approach that survives DST changes and data migrations without silent bugs.

### 3. Idempotent progress via `INSERT ... ON CONFLICT DO NOTHING`
The DB `UNIQUE(user_id, lesson_id)` constraint is the idempotency guarantee — not application-level check-then-insert. This is TOCTOU-safe under concurrent requests.

### 4. Separate `course_lessons` table for lesson count
Progress % denominator comes from the lesson manifest, not from counting progress rows. This gives correct results even when users skip lessons.

### 5. RTL readiness via CSS logical properties
No `margin-left`/`margin-right` in the codebase. All CSS uses `margin-inline-start`, `border-inline-start`, etc. Arabic layout works correctly by setting `document.documentElement.dir = 'rtl'` in `useLocale`.

### 6. Lazy locale loading (i18next-http-backend)
Locale JSON files load on demand, not bundled. Initial JS bundle stays small for low-bandwidth markets. Only the active locale is in memory.

---

## Testing

```bash
# Backend — fast unit tests (no DB, no Docker)
cd globalized-course-service
./mvnw test

# Backend — integration tests (requires Docker for Testcontainers)
./mvnw verify

# Frontend
cd globalized-course-ui
npm test -- --coverage
npm run typecheck
npm run lint
```

### What is tested

| Layer | What | How |
|---|---|---|
| `LocaleResolutionService` | All fallback chain paths, null/blank, dedup, Arabic | JUnit 5 + Mockito |
| `CoursePreviewService` | Happy path, 404 cases, duration formatting per locale | JUnit 5 + Mockito |
| `UserLocaleService` | GET defaults, upsert create/update, invalid IANA timezone | JUnit 5 + Mockito |
| `ProgressService` | New record, idempotent replay, 0%/100% edge, locale message | JUnit 5 + Mockito |
| Controllers | HTTP contract, 404/400 error format, validation | `@WebMvcTest` + MockMvc |
| `CoursePreviewCard` | Renders title/duration, fallback banner on/off, snapshot | RTL + Jest |
| `ProgressBar` | ARIA attrs, fill width, French message, snapshot | RTL + Jest |
| `LocaleSwitcher` | All 4 languages visible, current lang selected, snapshot | RTL + Jest |

---

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`):

1. **Backend unit tests** — no Docker required
2. **Backend integration tests** — Testcontainers spins up real Postgres
3. **Frontend typecheck** — `tsc --noEmit` (zero type errors required)
4. **Frontend lint** — ESLint with zero warnings allowed
5. **Frontend test** — Jest with coverage report
6. **Docker build** — images built for both services (not pushed on PRs)

Build fails on: test failure, lint errors, type errors.

---

## Trade-offs & Deliberate Non-Decisions

| Decision | Rationale |
|---|---|
| Single Spring Boot app (not microservices) | Complexity constraint. Clean layering provides modularity without Kubernetes overhead. |
| No Redis/caching | Premature. Add `@Cacheable` on `CoursePreviewService` if load tests show DB latency. |
| JWT not enforced | Auth delegated to API Gateway / Keycloak. One `@PreAuthorize` annotation adds it. |
| H2 for unit tests, Testcontainers for IT | H2 is fast for service-layer tests; Testcontainers runs real Postgres for repository tests. |
| i18next over react-intl | Larger ecosystem, lazy loading, simpler API, better for rapid locale expansion. |

---

## What Was Deliberately NOT Built

- **Video/media delivery** — out of scope; this module is metadata + progress
- **AI recommendations** — not justified by current requirements
- **Multi-tenancy** — `user_id` is opaque; Postgres RLS can be added later without schema change
- **Read replicas** — add a second `DataSource` bean when p99 read latency degrades
- **Message queue for progress** — add a Kafka consumer as a drop-in alternative to the POST endpoint

---

## Scaling Globally

1. **Stateless API** → deploy N replicas behind a load balancer (no sticky sessions)
2. **CDN cache** → `GET /courses/{id}/preview` is cacheable with `Vary: Accept-Language`
3. **Read replicas** → `@Transactional(readOnly=true)` routes to replica automatically
4. **Kafka progress ingestion** → POST endpoint → Kafka producer; consumer calls same ProgressService
5. **Locale file CDN** → move `/locales/*.json` to S3 + CloudFront for sub-50ms global delivery

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/coursedb` | PostgreSQL JDBC URL |
| `DB_USER` | `courseuser` | DB username |
| `DB_PASSWORD` | `coursepass` | DB password |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile (`dev` or `prod`) |
| `VITE_API_BASE_URL` | `''` (proxied) | Frontend API base URL |

---

## Supported Locales

| Code | Language | RTL | Backend Bundle | Frontend Bundle |
|---|---|---|---|---|
| `en` | English | No | `messages.properties` | `en/translation.json` |
| `fr` | French | No | `messages_fr.properties` | `fr/translation.json` |
| `ar` | Arabic | **Yes** | `messages_ar.properties` | `ar/translation.json` |
| `ja` | Japanese | No | `messages_ja.properties` | `ja/translation.json` |

To add a new locale: create a backend `.properties` file and a frontend `translation.json`. No code changes required.
