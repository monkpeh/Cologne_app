import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { Fragrance } from '../types';

function StarRating({ rating, onRate }: { rating?: number; onRate: (r: number) => void }) {
  const [hovered, setHovered] = useState(0);
  return (
    <div className="flex gap-0.5">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          onClick={() => onRate(n)}
          onMouseEnter={() => setHovered(n)}
          onMouseLeave={() => setHovered(0)}
          className="text-xl transition-colors"
          style={{ color: n <= (hovered || rating || 0) ? 'var(--ca-gold)' : 'var(--ca-surface-2)' }}
        >
          ★
        </button>
      ))}
    </div>
  );
}

export default function Collection() {
  const [fragrances, setFragrances] = useState<Fragrance[]>([]);
  const [ratings, setRatings]       = useState<Record<number, number>>({});
  const [suggestions, setSuggestions] = useState<Fragrance[]>([]);
  const [loading, setLoading]       = useState(true);

  const load = async () => {
    const { data } = await client.get('/collection');
    setFragrances(data.collection ?? []);
    setRatings(data.ratings ?? {});
    setSuggestions(data.suggestions ?? []);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleRemove = async (id: number) => {
    await client.delete(`/collection/${id}`);
    setFragrances((prev) => prev.filter((f) => f.id !== id));
  };

  const handleRate = async (id: number, rating: number) => {
    await client.post(`/collection/${id}/rate`, null, { params: { rating } });
    setRatings((prev) => ({ ...prev, [id]: rating }));
  };

  if (loading) return <Layout><p style={{ color: 'var(--ca-muted)' }}>Loading…</p></Layout>;

  return (
    <Layout>
      <h2 className="text-2xl font-semibold mb-6" style={{ color: 'var(--ca-gold)' }}>
        My Collection
      </h2>

      {fragrances.length === 0 ? (
        <div
          className="rounded-xl border p-10 text-center mb-8"
          style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
        >
          <p style={{ color: 'var(--ca-muted)' }}>
            Your collection is empty — browse the catalogue to add fragrances.
          </p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 mb-10">
          {fragrances.map((f) => (
            <div
              key={f.id}
              className="rounded-xl border p-5 flex flex-col gap-3"
              style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
            >
              {f.imageUrl && (
                <img src={f.imageUrl} alt={f.name} className="w-full h-36 object-cover rounded-lg" />
              )}
              <div>
                <p className="text-xs uppercase tracking-wider mb-0.5" style={{ color: 'var(--ca-muted)' }}>
                  {f.brand}
                </p>
                <p className="font-semibold" style={{ color: 'var(--ca-text)' }}>{f.name}</p>
                <p className="text-xs mt-1" style={{ color: 'var(--ca-muted)' }}>{f.scentFamily}</p>
              </div>

              <div className="flex flex-wrap gap-1.5 text-xs">
                {[f.projection, f.longevity].map((tag) => (
                  <span key={tag} className="rounded-full px-2 py-0.5"
                    style={{ background: 'var(--ca-surface-2)', color: 'var(--ca-muted)' }}>
                    {tag}
                  </span>
                ))}
                {f.officeSafe && (
                  <span className="rounded-full px-2 py-0.5"
                    style={{ background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}>
                    Office Safe
                  </span>
                )}
              </div>

              <StarRating rating={ratings[f.id]} onRate={(r) => handleRate(f.id, r)} />

              <button
                onClick={() => handleRemove(f.id)}
                className="text-xs self-start rounded px-2 py-1 transition-opacity hover:opacity-80"
                style={{ background: 'rgba(248,113,113,.1)', color: '#f87171' }}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      )}

      {suggestions.length > 0 && (
        <>
          <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--ca-text)' }}>
            You might also like
          </h3>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {suggestions.map((f) => (
              <div
                key={f.id}
                className="rounded-xl border p-4 flex flex-col gap-2"
                style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
              >
                <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.brand}</p>
                <p className="font-semibold text-sm" style={{ color: 'var(--ca-text)' }}>{f.name}</p>
                <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.scentFamily}</p>
              </div>
            ))}
          </div>
        </>
      )}
    </Layout>
  );
}
