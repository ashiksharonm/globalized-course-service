import React from 'react';
import { render, screen } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ProgressBar from './ProgressBar';
import type { ProgressResponse } from '../../api/types';

const testI18n = i18n.createInstance();
testI18n.use(initReactI18next).init({
    lng: 'en',
    resources: {
        en: {
            translation: {
                'progress.title': 'Your Progress',
                'progress.completed': '{{count}} of {{total}} lessons completed',
                'progress.record': 'Mark as complete',
            },
        },
        fr: {
            translation: {
                'progress.title': 'Votre progression',
                'progress.completed': '{{count}} leçon(s) sur {{total}} terminée(s)',
                'progress.record': 'Marquer comme terminé',
            },
        },
    },
    interpolation: { escapeValue: false },
});

const baseProgress: ProgressResponse = {
    courseId: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    userId: 'user-1',
    completedLessons: 3,
    totalLessons: 10,
    percentageComplete: 30,
    progressMessage: '30% complete — keep going!',
    locale: 'en',
};

function renderProgress(props: Partial<ProgressResponse> = {}) {
    const progress = { ...baseProgress, ...props };
    return render(
        <I18nextProvider i18n={testI18n}>
            <ProgressBar progress={progress} />
        </I18nextProvider>
    );
}

describe('ProgressBar', () => {
    test('renders progress title', () => {
        renderProgress();
        expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent('Your Progress');
    });

    test('renders progress message from server', () => {
        renderProgress();
        expect(screen.getByTestId('progress-message')).toHaveTextContent('30% complete — keep going!');
    });

    test('renders progress fill bar at correct width', () => {
        renderProgress({ percentageComplete: 50 });
        const fill = screen.getByTestId('progress-fill');
        expect(fill).toHaveStyle('width: 50%');
    });

    test('renders 0% fill correctly (no negative width)', () => {
        renderProgress({ percentageComplete: 0 });
        const fill = screen.getByTestId('progress-fill');
        expect(fill).toHaveStyle('width: 0%');
    });

    test('progress bar has correct ARIA attributes', () => {
        renderProgress({ percentageComplete: 30 });
        const bar = screen.getByRole('progressbar');
        expect(bar).toHaveAttribute('aria-valuenow', '30');
        expect(bar).toHaveAttribute('aria-valuemin', '0');
        expect(bar).toHaveAttribute('aria-valuemax', '100');
    });

    test('renders French progress message from server response', () => {
        renderProgress({ progressMessage: '30% terminé — continuez !', locale: 'fr' });
        expect(screen.getByTestId('progress-message')).toHaveTextContent('30% terminé — continuez !');
    });

    test('record button not rendered when onRecordLesson not provided', () => {
        renderProgress();
        expect(screen.queryByTestId('record-lesson-btn')).not.toBeInTheDocument();
    });

    test('matches snapshot', () => {
        const { container } = renderProgress();
        expect(container.firstChild).toMatchSnapshot();
    });
});
