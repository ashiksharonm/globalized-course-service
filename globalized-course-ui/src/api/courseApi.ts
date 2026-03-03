import apiClient from './apiClient';
import type { CoursePreviewResponse } from './types';

/**
 * Fetches a localized course preview.
 * The Accept-Language header is automatically set by the apiClient interceptor.
 */
export async function getCoursePreview(courseId: string): Promise<CoursePreviewResponse> {
    const { data } = await apiClient.get<CoursePreviewResponse>(`/api/v1/courses/${courseId}/preview`);
    return data;
}
