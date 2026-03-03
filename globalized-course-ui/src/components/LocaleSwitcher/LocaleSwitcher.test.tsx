import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LocaleSwitcher from './LocaleSwitcher';

const testI18n = i18n.createInstance();
testI18n.use(initReactI18next).init({
    lng: 'en',
    resources: {
        en: {
            translation: {
                'locale.switchLabel': 'Language',
                'locale.languages.en': 'English',
                'locale.languages.fr': 'Français',
                'locale.languages.ar': 'العربية',
                'locale.languages.ja': '日本語',
            },
        },
    },
    interpolation: { escapeValue: false },
});

function renderSwitcher(userId?: string) {
    return render(
        <I18nextProvider i18n={testI18n}>
            <LocaleSwitcher userId={userId} />
        </I18nextProvider>
    );
}

describe('LocaleSwitcher', () => {
    test('renders select control with language options', () => {
        renderSwitcher();
        const select = screen.getByRole('combobox');
        expect(select).toBeInTheDocument();
        expect(screen.getByText('English')).toBeInTheDocument();
        expect(screen.getByText('Français')).toBeInTheDocument();
        expect(screen.getByText('العربية')).toBeInTheDocument();
        expect(screen.getByText('日本語')).toBeInTheDocument();
    });

    test('shows current locale as selected', () => {
        renderSwitcher();
        const select = screen.getByRole('combobox') as HTMLSelectElement;
        expect(select.value).toBe('en');
    });

    test('Language label is rendered', () => {
        renderSwitcher();
        expect(screen.getByText('Language')).toBeInTheDocument();
    });

    test('matches snapshot', () => {
        const { container } = renderSwitcher();
        expect(container.firstChild).toMatchSnapshot();
    });
});
