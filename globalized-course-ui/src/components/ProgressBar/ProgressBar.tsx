import React from 'react';
import { useTranslation } from 'react-i18next';
import type { ProgressResponse } from '../../api/types';
import styles from './ProgressBar.module.css';

interface ProgressBarProps {
    progress: ProgressResponse;
    onRecordLesson?: (lessonId: string) => void;
    recording?: boolean;
}

/**
 * Displays a user's course progress with a visual progress bar and localized message.
 *
 * The progress message text comes from the API (generated server-side with correct locale),
 * while lesson count labels come from the i18next bundle.
 * This hybrid approach: server owns locale-sensitive % copy, client owns UI chrome.
 *
 * aria-valuenow / aria-valuemin / aria-valuemax ensure the bar is screen-reader accessible.
 */
const ProgressBar: React.FC<ProgressBarProps> = ({ progress, onRecordLesson, recording }) => {
    const { t } = useTranslation();

    return (
        <section className={styles.container} data-testid="progress-section">
            <h2 className={styles.title}>{t('progress.title')}</h2>

            <div
                className={styles.bar}
                role="progressbar"
                aria-valuenow={progress.percentageComplete}
                aria-valuemin={0}
                aria-valuemax={100}
                aria-label={progress.progressMessage}
            >
                <div
                    className={styles.fill}
                    style={{ width: `${progress.percentageComplete}%` }}
                    data-testid="progress-fill"
                />
            </div>

            <p className={styles.message} data-testid="progress-message">
                {progress.progressMessage}
            </p>

            <p className={styles.counts}>
                {t('progress.completed', {
                    count: progress.completedLessons,
                    total: progress.totalLessons,
                })}
            </p>

            {onRecordLesson && (
                <button
                    className={styles.recordButton}
                    onClick={() => onRecordLesson('lesson-next')}
                    disabled={recording}
                    data-testid="record-lesson-btn"
                >
                    {recording ? '...' : t('progress.record')}
                </button>
            )}
        </section>
    );
};

export default ProgressBar;
