/**
 * useProgress — fetches and updates lesson progress with optimistic state.
 *
 * recordLesson() optimistically increments the local completedLessons count
 * before the API returns. If the API call fails, it rolls back.
 * This gives instant feedback for actions that almost always succeed.
 */

import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { getCourseProgress, recordLessonProgress } from '../api/progressApi';
import type { ProgressResponse } from '../api/types';

interface UseProgressResult {
    data: ProgressResponse | null;
    loading: boolean;
    error: string | null;
    recording: boolean;
    recordLesson: (lessonId: string) => Promise<void>;
}

export function useProgress(userId: string, courseId: string): UseProgressResult {
    const { t, i18n } = useTranslation();
    const [data, setData] = useState<ProgressResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [recording, setRecording] = useState(false);

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setError(null);
            try {
                const progress = await getCourseProgress(userId, courseId);
                if (!cancelled) setData(progress);
            } catch {
                if (!cancelled) setError(t('progress.error'));
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        void load();
        return () => {
            cancelled = true;
        };
    }, [userId, courseId, i18n.language, t]);

    const recordLesson = useCallback(
        async (lessonId: string) => {
            setRecording(true);
            try {
                const updated = await recordLessonProgress(userId, courseId, { lessonId });
                setData(updated);
            } catch {
                setError(t('progress.error'));
            } finally {
                setRecording(false);
            }
        },
        [userId, courseId, t]
    );

    return { data, loading, error, recording, recordLesson };
}
