import React from 'react';
import { render, screen } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import CoursePreviewCard from './CoursePreviewCard';
import type { CoursePreviewResponse } from '../../api/types';

// Lightweight i18next instance for testing — no HTTP backend
const testI18n = i18n.createInstance();
testI18n.use(initReactI18next).init({
    lng: 'en',
    resources: {
        en: {
            translation: {
                'coursePreview.duration': 'Duration',
                'coursePreview.locale': 'Language',
                'coursePreview.fallbackNotice': 'Translation not available in your language. Showing English.',
            },
        },
        fr: {
            translation: {
                'coursePreview.duration': 'Durée',
                'coursePreview.locale': 'Langue',
                'coursePreview.fallbackNotice': 'La traduction n\'est pas disponible dans votre langue. Affichage en anglais.',
            },
        },
    },
    interpolation: { escapeValue: false },
});

const basePreview: CoursePreviewResponse = {
    courseId: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    title: 'Introduction to Machine Learning',
    description: 'Learn the basics of ML.',
    durationFormatted: '2h 30min',
    locale: 'en',
    fallbackUsed: false,
};

function renderCard(props: Partial<CoursePreviewResponse> = {}) {
    const preview = { ...basePreview, ...props };
    return render(
        <I18nextProvider i18n={testI18n}>
            <CoursePreviewCard preview={preview} />
        </I18nextProvider>
    );
}

describe('CoursePreviewCard', () => {
    test('renders course title', () => {
        renderCard();
        expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Introduction to Machine Learning');
    });

    test('renders duration correctly', () => {
        renderCard();
        expect(screen.getByTestId('duration')).toHaveTextContent('2h 30min');
    });

    test('renders locale code', () => {
        renderCard();
        expect(screen.getByTestId('locale')).toHaveTextContent('en');
    });

    test('does NOT show fallback banner when fallbackUsed is false', () => {
        renderCard({ fallbackUsed: false });
        expect(screen.queryByTestId('fallback-banner')).not.toBeInTheDocument();
    });

    test('shows fallback banner when fallbackUsed is true', () => {
        renderCard({ fallbackUsed: true });
        const banner = screen.getByTestId('fallback-banner');
        expect(banner).toBeInTheDocument();
        expect(banner).toHaveTextContent('Translation not available');
    });

    test('renders French locale code when locale is fr', () => {
        renderCard({ locale: 'fr', fallbackUsed: true });
        expect(screen.getByTestId('locale')).toHaveTextContent('fr');
    });

    test('matches snapshot', () => {
        const { container } = renderCard();
        expect(container.firstChild).toMatchSnapshot();
    });
});
