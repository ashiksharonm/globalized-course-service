import React from 'react';
import { useParams } from 'react-router-dom';
import { useCoursePreview } from '../hooks/useCoursePreview';
import { useProgress } from '../hooks/useProgress';
import { useTranslation } from 'react-i18next';
import CoursePreviewCard from '../components/CoursePreviewCard/CoursePreviewCard';
import ProgressBar from '../components/ProgressBar/ProgressBar';
import LocaleSwitcher from '../components/LocaleSwitcher/LocaleSwitcher';
import styles from './CoursePreviewPage.module.css';

const DEFAULT_COURSE_ID = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
const DEFAULT_USER_ID = 'demo-user-1';

/**
 * Course preview page — composes all feature components.
 *
 * In production, courseId comes from the route param and userId from an auth context.
 * The DEFAULT values support running the frontend standalone without auth for demos.
 */
const CoursePreviewPage: React.FC = () => {
    const { courseId = DEFAULT_COURSE_ID } = useParams<{ courseId: string }>();
    const { t } = useTranslation();

    const { data: preview, loading: previewLoading, error: previewError } = useCoursePreview(courseId);
    const { data: progress, loading: progressLoading, error: progressError, recording, recordLesson } =
        useProgress(DEFAULT_USER_ID, courseId);

    return (
        <main className={styles.page}>
            <header className={styles.header}>
                <h1 className={styles.appTitle}>Globalized Learning Platform</h1>
                <LocaleSwitcher userId={DEFAULT_USER_ID} />
            </header>

            <section className={styles.content}>
                {previewLoading && <p>{t('coursePreview.loading')}</p>}
                {previewError && <p className={styles.error}>{previewError}</p>}
                {preview && <CoursePreviewCard preview={preview} />}

                {progressLoading && <p>{t('progress.loading')}</p>}
                {progressError && <p className={styles.error}>{progressError}</p>}
                {progress && (
                    <ProgressBar
                        progress={progress}
                        onRecordLesson={recordLesson}
                        recording={recording}
                    />
                )}
            </section>
        </main>
    );
};

export default CoursePreviewPage;
