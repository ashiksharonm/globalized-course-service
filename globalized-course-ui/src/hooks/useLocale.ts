/**
 * useLocale — manages the active locale and synchronizes it across:
 * 1. i18next (UI translations)
 * 2. document.documentElement.dir (RTL/LTR for Arabic, Hebrew)
 * 3. document.documentElement.lang (HTML lang attribute for screen readers + SEO)
 * 4. Backend user locale preference (persisted server-side)
 *
 * RTL detection is explicit and allowlist-based — we identify known RTL languages
 * rather than detecting dynamically, to avoid incorrect RTL for edge-case locales.
 */

import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { updateUserLocale } from '../api/localeApi';

const RTL_LANGUAGES = new Set(['ar', 'he', 'fa', 'ur']);
const IANA_TIMEZONE_MAP: Record<string, string> = {
    en: 'UTC',
    fr: 'Europe/Paris',
    ar: 'Asia/Riyadh',
    ja: 'Asia/Tokyo',
};

interface UseLocaleResult {
    currentLanguage: string;
    isRTL: boolean;
    changeLocale: (language: string, userId?: string) => Promise<void>;
    supportedLanguages: string[];
}

export function useLocale(): UseLocaleResult {
    const { i18n } = useTranslation();
    const currentLanguage = i18n.language ?? 'en';
    const isRTL = RTL_LANGUAGES.has(currentLanguage.split('-')[0]);

    const changeLocale = useCallback(
        async (language: string, userId?: string) => {
            // 1. Switch i18next language (loads locale bundle if not cached)
            await i18n.changeLanguage(language);

            // 2. Update HTML dir and lang for RTL support and accessibility
            const primaryTag = language.split('-')[0];
            document.documentElement.dir = RTL_LANGUAGES.has(primaryTag) ? 'rtl' : 'ltr';
            document.documentElement.lang = language;

            // 3. Persist to backend if a userId is available
            if (userId) {
                try {
                    await updateUserLocale(userId, {
                        language: primaryTag,
                        timezone: IANA_TIMEZONE_MAP[primaryTag] ?? 'UTC',
                    });
                } catch {
                    // Non-fatal: UI is already updated. Backend sync failure should not interrupt UX.
                    console.warn('[useLocale] Failed to persist locale preference to backend.');
                }
            }
        },
        [i18n]
    );

    return {
        currentLanguage,
        isRTL,
        changeLocale,
        supportedLanguages: ['en', 'fr', 'ar', 'ja'],
    };
}
