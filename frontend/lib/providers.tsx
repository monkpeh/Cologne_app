'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { getCurrentUser } from './api/auth';
import { useAuthStore } from './store/authStores';

const queryClient = new QueryClient();

export function Providers({ children }: { children: React.ReactNode }) {
    const { setCurrentUser, setLoading } = useAuthStore();

    useEffect(() => {
        getCurrentUser()
            .then(setCurrentUser)
            .catch(() => setCurrentUser(null))
            .finally(() => setLoading(false));
    }, []);

    return (
        <QueryClientProvider client={queryClient}>
            {children}
        </QueryClientProvider>
    );
}