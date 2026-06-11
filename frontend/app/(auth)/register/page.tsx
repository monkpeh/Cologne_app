'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { register } from '@/lib/api/auth';

export default function RegisterPage() {
    const router = useRouter();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError('');
        if (password.length < 6) {
            setError('Password must be at least 6 characters.');
            return;
        }
        setIsLoading(true);
        try {
            await register(username, password);
            router.push('/login?registered=true');
        } catch {
            setError('Registration failed. Username may already be taken.');
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="bg-neutral-900 rounded-xl p-8 shadow-xl border border-neutral-800">
            <h2 className="text-xl font-semibold text-white mb-6">Create account</h2>
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
                    <p className="text-neutral-600 text-xs mt-1">Minimum 6 characters</p>
                </div>
                {error && <p className="text-red-400 text-sm">{error}</p>}
                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-amber-400 hover:bg-amber-300 text-neutral-950 font-semibold rounded-lg py-2 transition-colors disabled:opacity-50"
                >
                    {isLoading ? 'Creating account...' : 'Create account'}
                </button>
            </form>
            <p className="text-neutral-500 text-sm text-center mt-4">
                Already have an account?{' '}
                <Link href="/login" className="text-amber-400 hover:underline">
                    Sign in
                </Link>
            </p>
        </div>
    );
}