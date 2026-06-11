'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore, useIsAdmin } from '@/lib/store/authStores';
import Link from 'next/link';
import { logout } from '@/lib/api/auth';

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
    const router = useRouter();
    const { currentUser, isLoading, setCurrentUser } = useAuthStore();
    const isAdmin = useIsAdmin();

    useEffect(() => {
        if (!isLoading && !currentUser) {
            router.push('/login');
        }
    }, [isLoading, currentUser, router]);

    if (isLoading) {
        return (
            <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
                <p className="text-neutral-400">Loading...</p>
            </div>
        );
    }

    if (!currentUser) return null;

    async function handleLogout() {
        await logout();
        setCurrentUser(null);
        router.push('/login');
    }

    return (
        <div className="min-h-screen bg-neutral-950 flex">
            {/* Sidebar */}
            <aside className="w-56 bg-neutral-900 border-r border-neutral-800 flex flex-col p-4">
                <h1 className="text-amber-400 font-bold text-lg mb-8">Cologne Advisor</h1>
                <nav className="flex flex-col gap-1 flex-1">
                    <Link href="/collection" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                        Collection
                    </Link>

                    <Link href="/browse" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                        Browse
                    </Link>
                    <Link href="/recommend" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                        Recommend
                    </Link>
                    <Link href="/stats" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                        Stats
                    </Link>
                    <Link href="/stats" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                        Stats
                    </Link>
                    {isAdmin && (
                        <Link href="/admin" className="text-neutral-300 hover:text-amber-400 hover:bg-neutral-800 rounded-lg px-3 py-2 text-sm transition-colors">
                            Admin
                        </Link>
                    )}
                </nav>
                <div className="border-t border-neutral-800 pt-4">
                    <p className="text-neutral-500 text-xs mb-2">{currentUser.username}</p>
                    <button
                        onClick={"/logout"}
                        className="text-neutral-400 hover:text-red-400 text-sm transition-colors"
                    >
                        Sign out
                    </button>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 p-8 overflow-auto">
                {children}
            </main>
        </div>
    );
}