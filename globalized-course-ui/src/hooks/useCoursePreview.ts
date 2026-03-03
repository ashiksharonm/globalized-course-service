/**
 * useCoursePreview — fetches and manages course preview state.
 *
 * This hook re-fetches whenever `courseId` or `i18n.language` changes,
 * ensuring the displayed content updates immediately when the user switches locale.
 * The Accept-Language header is handled by the apiClient interceptor — this hook
 * just needs to know when to trigger a re-fetch.
 */

import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { getCoursePreview } from '../api/courseApi';
import type { CoursePreviewResponse } from '../api/types';

interface UseCoursePreviewResult {
    data: CoursePreviewResponse | null;
    loading: boolean;
    error: string | null;
    refetch: () => void;
}

export function useCoursePreview(courseId: string): UseCoursePreviewResult {
    const { t, i18n } = useTranslation();
    const [data, setData] = useState<CoursePreviewResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [refresh, setRefresh] = useState(0);

    useEffect(() => {
        let cancelled = false;

        async function fetch() {
            setLoading(true);
            setError(null);
            try {
                const preview = await getCoursePreview(courseId);
                if (!cancelled) setData(preview);
            } catch {
                if (!cancelled) setError(t('coursePreview.error'));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        void fetch();

        return () => {
            cancelled = true;
        };
        // Re-fetch when locale changes — ensures localized content is fresh
    }, [courseId, i18n.language, refresh, t]);

    return {
        data,
        loading,
        error,
        refetch: () => setRefresh((prev) => prev + 1),
    };
}
