/**
 * i18next initialization for the Globalized Course UI.
 *
 * Configuration decisions:
 * - `i18next-http-backend`: Lazily loads locale JSON files from /locales/{lng}/translation.json.
 *   This keeps the initial JS bundle small, which matters for low-bandwidth users in emerging markets.
 * - `i18next-browser-languagedetector`: Reads from localStorage → navigator.language → html tag.
 *   Priority order ensures a user's saved preference always wins.
 * - `fallbackLng: 'en'`: Mirrors the backend's base locale. If a key is missing in the active locale,
 *   the English string is shown rather than a raw key — no blank UI.
 * - `interpolation.escapeValue: false`: React already escapes values by default; disabling here
 *   prevents double-escaping of HTML entities (e.g. in French "continuez !").
 */

import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import HttpBackend from 'i18next-http-backend';

i18n
    .use(HttpBackend)
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        fallbackLng: 'en',
        supportedLngs: ['en', 'fr', 'ar', 'ja'],
        debug: import.meta.env.DEV,

        detection: {
            // Order of sources for language detection
            order: ['localStorage', 'navigator', 'htmlTag'],
            // Key used to persist choice in localStorage
            lookupLocalStorage: 'i18nextLng',
            cacheUserLanguage: true,
        },

        backend: {
            loadPath: '/locales/{{lng}}/translation.json',
        },

        interpolation: {
            escapeValue: false,
        },

        react: {
            // Wait for i18n to be ready before rendering — prevents untranslated flash on load
            useSuspense: true,
        },
    });

export default i18n;
