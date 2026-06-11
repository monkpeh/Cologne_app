import { useState } from 'react'; import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { useAuthStore } from '../store/authStore';
import type { AuthResponse } from '../types';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);
  const { login }               = useAuthStore();
  const navigate                = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await client.post<AuthResponse>('/auth/login', { username, password });
      login(data.token, data.username, data.role);
      navigate('/collection');
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Invalid credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4" style={{ background: 'var(--ca-bg)' }}>
      <div
        className="w-full max-w-sm rounded-xl p-8 border"
        style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
      >
        <h1
          className="text-center text-2xl font-semibold tracking-widest uppercase mb-8"
          style={{ color: 'var(--ca-gold)' }}
        >
          Cologne Advisor
        </h1>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
              Username
            </label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="rounded px-3 py-2 text-sm outline-none focus:ring-1"
              style={{
                background: 'var(--ca-surface-2)',
                color: 'var(--ca-text)',
                border: '1px solid var(--ca-border)',
                // @ts-ignore
                '--tw-ring-color': 'var(--ca-gold)',
              }}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
              Password
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="rounded px-3 py-2 text-sm outline-none focus:ring-1"
              style={{
                background: 'var(--ca-surface-2)',
                color: 'var(--ca-text)',
                border: '1px solid var(--ca-border)',
              }}
            />
          </div>

          {error && (
            <p className="text-sm text-center" style={{ color: '#f87171' }}>
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="mt-2 rounded py-2 text-sm font-semibold uppercase tracking-wider transition-opacity hover:opacity-80 disabled:opacity-50"
            style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
          >
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className="text-center text-sm mt-6" style={{ color: 'var(--ca-muted)' }}>
          No account?{' '}
          <Link to="/register" style={{ color: 'var(--ca-gold)' }}>
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}
