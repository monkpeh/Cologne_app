import { create } from 'zustand';
import { AppUser } from '../api/types';

interface AuthState {
    currentUser: AppUser | null;
    isLoading: boolean;
    setCurrentUser: (user: AppUser | null) => void;
    setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    currentUser: null,
    isLoading: true,
    setCurrentUser: (user) => set({ currentUser: user }),
    setLoading: (loading) => set({ isLoading: loading }),
}));

export const useCurrentUser = () => useAuthStore((state) => state.currentUser);
export const useIsAuthenticated = () => useAuthStore((state) => state.currentUser !== null);
export const useIsAdmin = () => useAuthStore((state) => state.currentUser?.role === 'ADMIN');