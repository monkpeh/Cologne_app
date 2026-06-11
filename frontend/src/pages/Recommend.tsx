import { useEffect, useState } from 'react'; import type { FormEvent } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { MetaOption, RecommendationResult } from '../types';

export default function Recommend() {
  const [weathers, setWeathers]   = useState<MetaOption[]>([]);
  const [occasions, setOccasions] = useState<MetaOption[]>([]);
  const [weather, setWeather]     = useState('');
  const [occasion, setOccasion]   = useState('');
  const [results, setResults]     = useState<RecommendationResult[]>([]);
  const [loading, setLoading]     = useState(false);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    client.get('/fragrances/meta').then(({ data }) => {
      setWeathers(data.weathers);
      setOccasions(data.occasions);
      if (data.weathers.length)  setWeather(data.weathers[0].value);
      if (data.occasions.length) setOccasion(data.occasions[0].value);
    });
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const { data } = await client.post('/recommend', { weather, occasion });
    setResults(data.results);
    setSubmitted(true);
    setLoading(false);
  };

  const selectStyle = {
    background: 'var(--ca-surface)',
    color: 'var(--ca-text)',
    border: '1px solid var(--ca-border)',
  };

  return (
    <Layout>
      <h2 className="text-2xl font-semibold mb-6" style={{ color: 'var(--ca-gold)' }}>
        Get a Recommendation
      </h2>

      <form
        onSubmit={handleSubmit}
        className="rounded-xl border p-6 mb-8 flex flex-wrap gap-4 items-end"
        style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
      >
        <div className="flex flex-col gap-1 flex-1 min-w-[160px]">
          <label className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
            Weather
          </label>
          <select
            value={weather}
            onChange={(e) => setWeather(e.target.value)}
            className="rounded px-3 py-2 text-sm outline-none"
            style={selectStyle}
          >
            {weathers.map((w) => <option key={w.value} value={w.value}>{w.label}</option>)}
          </select>
        </div>

        <div className="flex flex-col gap-1 flex-1 min-w-[160px]">
          <label className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
            Occasion
          </label>
          <select
            value={occasion}
            onChange={(e) => setOccasion(e.target.value)}
            className="rounded px-3 py-2 text-sm outline-none"
            style={selectStyle}
          >
            {occasions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="rounded px-6 py-2 text-sm font-semibold uppercase tracking-wider transition-opacity hover:opacity-80 disabled:opacity-50"
          style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
        >
          {loading ? 'Finding…' : 'Recommend'}
        </button>
      </form>

      {submitted && results.length === 0 && (
        <p style={{ color: 'var(--ca-muted)' }}>
          No recommendations — add fragrances to your collection first.
        </p>
      )}

      <div className="flex flex-col gap-4">
        {results.map(({ fragrance, score, reasons }, i) => (
          <div
            key={fragrance.id}
            className="rounded-xl border p-5 flex gap-5"
            style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
          >
            <div
              className="text-3xl font-bold w-10 shrink-0 text-center"
              style={{ color: 'var(--ca-gold)' }}
            >
              #{i + 1}
            </div>
            <div className="flex-1">
              <p className="text-xs uppercase tracking-wider mb-0.5" style={{ color: 'var(--ca-muted)' }}>
                {fragrance.brand}
              </p>
              <p className="font-semibold mb-1" style={{ color: 'var(--ca-text)' }}>{fragrance.name}</p>
              <ul className="flex flex-col gap-1">
                {reasons.map((r, j) => (
                  <li key={j} className="text-sm flex gap-2" style={{ color: 'var(--ca-muted)' }}>
                    <span style={{ color: 'var(--ca-gold)' }}>✓</span> {r}
                  </li>
                ))}
              </ul>
            </div>
            <div className="text-right shrink-0">
              <p className="text-xs uppercase tracking-wider mb-1" style={{ color: 'var(--ca-muted)' }}>Score</p>
              <p className="text-xl font-bold" style={{ color: 'var(--ca-gold)' }}>{score}</p>
            </div>
          </div>
        ))}
      </div>
    </Layout>
  );
}
