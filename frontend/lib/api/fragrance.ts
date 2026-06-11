import client from './client';
import { Fragrance, ScoredFragrance, Weather, Occasion } from './types';

// ── Catalogue ──────────────────────────────────────────────────────────────

export async function getAllFragrances(): Promise<Fragrance[]> {
    const response = await client.get<Fragrance[]>('/api/fragrances');
    return response.data;
}

export async function getFragrance(id: number): Promise<Fragrance> {
    const response = await client.get<Fragrance>(`/api/fragrances/${id}`);
    return response.data;
}

export async function searchFragrances(q: string): Promise<Fragrance[]> {
    const response = await client.get<Fragrance[]>('/api/fragrances/search', { params: { q } });
    return response.data;
}

export async function compareFragrances(ids: number[]): Promise<Fragrance[]> {
    const response = await client.get<Fragrance[]>('/api/fragrances/compare', { params: { ids } });
    return response.data;
}

// ── Collection ─────────────────────────────────────────────────────────────

export async function getCollection(): Promise<Fragrance[]> {
    const response = await client.get<Fragrance[]>('/api/users/me/collection');
    return response.data;
}

export async function addToCollection(id: number): Promise<void> {
    await client.post(`/api/users/me/collection/${id}`);
}

export async function removeFromCollection(id: number): Promise<void> {
    await client.delete(`/api/users/me/collection/${id}`);
}

export async function rateFragrance(id: number, rating: number): Promise<void> {
    await client.post(`/api/users/me/collection/${id}/rate`, { rating });
}

// ── Recommendations ────────────────────────────────────────────────────────

export async function getRecommendations(
    weather: Weather,
    occasion: Occasion
): Promise<ScoredFragrance[]> {
    const response = await client.get<ScoredFragrance[]>('/api/users/me/recommendations', {
        params: { weather, occasion },
    });
    return response.data;
}

export async function getSuggestions(): Promise<Fragrance[]> {
    const response = await client.get<Fragrance[]>('/api/users/me/suggestions');
    return response.data;
}