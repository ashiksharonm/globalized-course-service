import apiClient from './apiClient';
import type { UserLocaleResponse, LocalePreferenceRequest } from './types';

export async function getUserLocale(userId: string): Promise<UserLocaleResponse> {
    const { data } = await apiClient.get<UserLocaleResponse>(`/api/v1/users/${userId}/locale`);
    return data;
}

export async function updateUserLocale(
    userId: string,
    request: LocalePreferenceRequest
): Promise<UserLocaleResponse> {
    const { data } = await apiClient.put<UserLocaleResponse>(`/api/v1/users/${userId}/locale`, request);
    return data;
}
