import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { UserStats, Fragrance } from '../types';

export default function Stats() {
  const [stats, setStats]     = useState<UserStats | null>(null);
  const [ratings, setRatings] = useState<Record<number, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/stats').then(({ data }) => {
      setStats(data.stats);
      setRatings(data.ratings);
      setLoading(false);
    });
  }, []);

  if (loading) return <Layout><p style={{ color: 'var(--ca-muted)' }}>Loading…</p></Layout>;
  if (!stats)  return <Layout><p style={{ color: 'var(--ca-muted)' }}>No data yet.</p></Layout>;

  const card = (label: string, value: string | number) => (
    <div
      className="rounded-xl border p-5 flex flex-col gap-1"
      style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
    >
      <p className="text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>{label}</p>
      <p className="text-3xl font-bold" style={{ color: 'var(--ca-gold)' }}>{value}</p>
    </div>
  );

  return (
    <Layout>
      <h2 className="text-2xl font-semibold mb-6" style={{ color: 'var(--ca-gold)' }}>
        Collection Stats
      </h2>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 mb-8">
        {card('Total Fragrances', stats.totalFragrances)}
        {card('Average Rating', stats.averageRating > 0 ? `${stats.averageRating.toFixed(1)} ★` : '—')}
        {card('Scent Families', Object.keys(stats.scentFamilyBreakdown).length)}
      </div>

      {/* Scent family breakdown */}
      {Object.keys(stats.scentFamilyBreakdown).length > 0 && (
        <Section title="Scent Family Breakdown">
          <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            {Object.entries(stats.scentFamilyBreakdown).map(([family, count]) => (
              <div
                key={family}
                className="rounded-lg border px-4 py-3 flex justify-between items-center"
                style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
              >
                <span className="text-sm" style={{ color: 'var(--ca-text)' }}>{family}</span>
                <span className="font-bold" style={{ color: 'var(--ca-gold)' }}>{count}</span>
              </div>
            ))}
          </div>
        </Section>
      )}

      {/* Top rated */}
      {stats.topRated.length > 0 && (
        <Section title="Top Rated">
          <FragranceList fragrances={stats.topRated} ratings={ratings} />
        </Section>
      )}

      {/* Standouts */}
      {(stats.highestProjection || stats.longestLasting) && (
        <Section title="Standouts">
          <div className="grid gap-4 sm:grid-cols-2">
            {stats.highestProjection && (
              <Standout label="Highest Projection" fragrance={stats.highestProjection} />
            )}
            {stats.longestLasting && (
              <Standout label="Longest Lasting" fragrance={stats.longestLasting} />
            )}
          </div>
        </Section>
      )}
    </Layout>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="mb-8">
      <h3 className="text-lg font-semibold mb-3" style={{ color: 'var(--ca-text)' }}>{title}</h3>
      {children}
    </div>
  );
}

function FragranceList({ fragrances, ratings }: { fragrances: Fragrance[]; ratings: Record<number, number> }) {
  return (
    <div className="flex flex-col gap-2">
      {fragrances.map((f) => (
        <div
          key={f.id}
          className="rounded-lg border px-4 py-3 flex justify-between items-center"
          style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
        >
          <div>
            <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.brand}</p>
            <p className="text-sm font-semibold" style={{ color: 'var(--ca-text)' }}>{f.name}</p>
          </div>
          {ratings[f.id] && (
            <span className="text-sm font-bold" style={{ color: 'var(--ca-gold)' }}>
              {'★'.repeat(ratings[f.id])}
            </span>
          )}
        </div>
      ))}
    </div>
  );
}

function Standout({ label, fragrance }: { label: string; fragrance: Fragrance }) {
  return (
    <div
      className="rounded-xl border p-5"
      style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
    >
      <p className="text-xs uppercase tracking-wider mb-2" style={{ color: 'var(--ca-muted)' }}>{label}</p>
      <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{fragrance.brand}</p>
      <p className="font-semibold" style={{ color: 'var(--ca-gold)' }}>{fragrance.name}</p>
    </div>
  );
}
