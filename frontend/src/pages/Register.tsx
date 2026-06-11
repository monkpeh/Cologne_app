import { useState } from 'react'; import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { useAuthStore } from '../store/authStore';
import type { AuthResponse } from '../types';

export default function Register() {
  const [username, setUsername]               = useState('');
  const [password, setPassword]               = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError]                     = useState('');
  const [loading, setLoading]                 = useState(false);
  const { login }                             = useAuthStore();
  const navigate                              = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      const { data } = await client.post<AuthResponse>('/auth/register', {
        username,
        password,
        confirmPassword,
      });
      login(data.token, data.username, data.role);
      navigate('/collection');
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const field = (
    label: string,
    type: string,
    value: string,
    onChange: (v: string) => void
  ) => (
    <div className="flex flex-col gap-1">
      <label className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
        {label}
      </label>
      <input
        type={type}
        required
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="rounded px-3 py-2 text-sm outline-none focus:ring-1"
        style={{
          background: 'var(--ca-surface-2)',
          color: 'var(--ca-text)',
          border: '1px solid var(--ca-border)',
        }}
      />
    </div>
  );

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
          Create Account
        </h1>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {field('Username', 'text', username, setUsername)}
          {field('Password', 'password', password, setPassword)}
          {field('Confirm Password', 'password', confirmPassword, setConfirmPassword)}

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
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>

        <p className="text-center text-sm mt-6" style={{ color: 'var(--ca-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--ca-gold)' }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
