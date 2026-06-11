import { create } from 'zustand';

interface AuthState {
  token: string | null;
  username: string | null;
  role: string | null;
  isAdmin: () => boolean;
  login: (token: string, username: string, role: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token:    localStorage.getItem('jwt'),
  username: localStorage.getItem('username'),
  role:     localStorage.getItem('role'),

  isAdmin: () => get().role === 'ADMIN',

  login: (token, username, role) => {
    localStorage.setItem('jwt', token);
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
    set({ token, username, role });
  },

  logout: () => {
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    set({ token: null, username: null, role: null });
  },
}));
