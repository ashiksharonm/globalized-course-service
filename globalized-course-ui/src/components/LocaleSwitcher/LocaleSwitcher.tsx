import React from 'react';
import { useTranslation } from 'react-i18next';
import { useLocale } from '../../hooks/useLocale';
import styles from './LocaleSwitcher.module.css';

interface LocaleSwitcherProps {
    userId?: string;
}

/**
 * Locale switcher control.
 *
 * When the user picks a language:
 * 1. i18next immediately loads the new locale bundle (lazy) — no page reload.
 * 2. document.documentElement.dir is set to 'rtl'/'ltr' via useLocale.
 * 3. If userId is provided, the preference is persisted to the backend.
 *
 * Why a <select> not buttons: a dropdown scales to 50+ languages without layout issues.
 * Language names are displayed in their own language (e.g. "Français" not "French")
 * per i18n best practice — users should recognize their language visually.
 */
const LocaleSwitcher: React.FC<LocaleSwitcherProps> = ({ userId }) => {
    const { t } = useTranslation();
    const { currentLanguage, changeLocale, supportedLanguages } = useLocale();

    const handleChange = async (event: React.ChangeEvent<HTMLSelectElement>) => {
        await changeLocale(event.target.value, userId);
    };

    return (
        <div className={styles.wrapper} data-testid="locale-switcher">
            <label htmlFor="locale-select" className={styles.label}>
                {t('locale.switchLabel')}
            </label>
            <select
                id="locale-select"
                className={styles.select}
                value={currentLanguage.split('-')[0]}
                onChange={handleChange}
                aria-label={t('locale.switchLabel')}
            >
                {supportedLanguages.map((lang) => (
                    <option key={lang} value={lang}>
                        {t(`locale.languages.${lang}`)}
                    </option>
                ))}
            </select>
        </div>
    );
};

export default LocaleSwitcher;
