'use client';

import { useSearchParams } from 'next/navigation';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { login, getCurrentUser } from '@/lib/api/auth';
import { useAuthStore } from '@/lib/store/authStores';

export default function LoginPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const registered = searchParams.get('registered');
    const { setCurrentUser } = useAuthStore();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError('');
        setIsLoading(true);
        try {
            await login(username, password);
            const user = await getCurrentUser();
            setCurrentUser(user);
            router.push('/collection');
        } catch {
            setError('Invalid username or password.');
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="bg-neutral-900 rounded-xl p-8 shadow-xl border border-neutral-800">
            <h2 className="text-xl font-semibold text-white mb-6">Sign in</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm text-neutral-400 mb-1">Username</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                        className="w-full bg-neutral-800 border border-neutral-700 rounded-lg px-3 py-2 text-white placeholder-neutral-500 focus:outline-none focus:border-amber-400"
                    />
                </div>
                <div>
                    <label className="block text-sm text-neutral-400 mb-1">Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        className="w-full bg-neutral-800 border border-neutral-700 rounded-lg px-3 py-2 text-white placeholder-neutral-500 focus:outline-none focus:border-amber-400"
                    />
                </div>
                {error && <p className="text-red-400 text-sm">{error}</p>}
                {registered && <p className="text-green-400 text-sm">Account created! Please sign in.</p>}
                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-amber-400 hover:bg-amber-300 text-neutral-950 font-semibold rounded-lg py-2 transition-colors disabled:opacity-50"
                >
                    {isLoading ? 'Signing in...' : 'Sign in'}
                </button>
            </form>
            <p className="text-neutral-500 text-sm text-center mt-4">
                No account?{' '}
                <Link href="/register" className="text-amber-400 hover:underline">
                    Register
                </Link>
            </p>
        </div>
    );
}