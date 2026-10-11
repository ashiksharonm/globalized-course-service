/** Vite environment access, isolated so Jest (CommonJS) can mock it. */
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '';
export const IS_DEV: boolean = import.meta.env.DEV;
