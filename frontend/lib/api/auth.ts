import client from './client';
import { AppUser } from './types';

export async function login(username: string, password: string): Promise<void> {
    const params = new URLSearchParams({ username, password });
    await client.post('/login', params);
}

export async function register(username: string, password: string): Promise<void> {
    const params = new URLSearchParams({ username, password, confirmPassword: password });
    await client.post('/register', params);
}

export async function logout(): Promise<void> {
    await client.post('/logout');
}

export async function getCurrentUser(): Promise<AppUser> {
    const response = await client.get<AppUser>('/api/users/me');
    return response.data;
}