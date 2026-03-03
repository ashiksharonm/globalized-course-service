/**
 * Type definitions for the Course Preview API.
 * Mirrors the backend CoursePreviewResponse DTO exactly.
 * If the backend contract changes, update this type first — it's the single source of truth on the frontend.
 */
export interface CoursePreviewResponse {
    courseId: string;
    title: string;
    description: string;
    durationFormatted: string;
    locale: string;
    fallbackUsed: boolean;
}

export interface UserLocaleResponse {
    userId: string;
    language: string;
    region: string | null;
    timezone: string;
    updatedAt: string | null;
}

export interface LocalePreferenceRequest {
    language: string;
    region?: string;
    timezone: string;
}

export interface ProgressResponse {
    courseId: string;
    userId: string;
    completedLessons: number;
    totalLessons: number;
    percentageComplete: number;
    progressMessage: string;
    locale: string;
}

export interface ProgressEventRequest {
    lessonId: string;
}

/** RFC 7807 Problem Detail — error response from the backend */
export interface ApiError {
    status: number;
    title: string;
    detail: string;
    type: string;
    timestamp: string;
}
