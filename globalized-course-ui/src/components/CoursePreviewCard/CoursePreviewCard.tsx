import React from 'react';
import { useTranslation } from 'react-i18next';
import type { CoursePreviewResponse } from '../../api/types';
import styles from './CoursePreviewCard.module.css';

interface CoursePreviewCardProps {
    preview: CoursePreviewResponse;
}

/**
 * Displays a localized course preview card.
 *
 * All display strings come from either the API response (title, description, duration)
 * or i18next translation keys (labels, notices). No hardcoded English strings.
 *
 * The fallback notice banner is shown only when the backend signals that a translation
 * was unavailable in the requested locale — this gives transparent UX to global users.
 */
const CoursePreviewCard: React.FC<CoursePreviewCardProps> = ({ preview }) => {
    const { t } = useTranslation();

    return (
        <article className={styles.card} data-testid="course-preview-card">
            {preview.fallbackUsed && (
                <div className={styles.fallbackBanner} role="alert" data-testid="fallback-banner">
                    {t('coursePreview.fallbackNotice')}
                </div>
            )}

            <h1 className={styles.title}>{preview.title}</h1>
            <p className={styles.description}>{preview.description}</p>

            <dl className={styles.metadata}>
                <dt>{t('coursePreview.duration')}</dt>
                <dd data-testid="duration">{preview.durationFormatted}</dd>

                <dt>{t('coursePreview.locale')}</dt>
                <dd data-testid="locale">{preview.locale}</dd>
            </dl>
        </article>
    );
};

export default CoursePreviewCard;
