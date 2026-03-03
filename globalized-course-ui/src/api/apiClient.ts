/**
 * Centralized Axios instance for all backend API calls.
 *
 * Design decisions:
 * - baseURL reads from VITE_API_BASE_URL env variable, defaulting to '' (proxied through Vite in dev).
 * - Accept-Language header is injected dynamically per request via an interceptor,
 *   reading from i18next's currently active language. This ensures every API call
 *   carries the user's current locale without any per-call boilerplate.
 * - Errors are thrown as-is; individual hooks decide how to handle them.
 */

import axios from 'axios';
import i18n from '../i18n';

const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10_000,
});

// Inject Accept-Language on every outgoing request
apiClient.interceptors.request.use((config) => {
    const language = i18n.language ?? 'en';
    config.headers['Accept-Language'] = language;
    return config;
});

export default apiClient;
