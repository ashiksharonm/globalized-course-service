import apiClient from './apiClient';
import type { ProgressResponse, ProgressEventRequest } from './types';

export async function getCourseProgress(userId: string, courseId: string): Promise<ProgressResponse> {
    const { data } = await apiClient.get<ProgressResponse>(
        `/api/v1/users/${userId}/courses/${courseId}/progress`
    );
    return data;
}

export async function recordLessonProgress(
    userId: string,
    courseId: string,
    request: ProgressEventRequest
): Promise<ProgressResponse> {
    const { data } = await apiClient.post<ProgressResponse>(
        `/api/v1/users/${userId}/courses/${courseId}/progress`,
        request
    );
    return data;
}
