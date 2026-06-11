import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { Fragrance, CollectionEntry } from '../types';

export default function Compare() {
  const [collection, setCollection] = useState<CollectionEntry[]>([]);
  const [selected, setSelected]     = useState<number[]>([]);
  const [compared, setCompared]     = useState<{ fragrances: Fragrance[]; ratings: Record<number, number> } | null>(null);
  const [loading, setLoading]       = useState(true);
  const [comparing, setComparing]   = useState(false);

  useEffect(() => {
    client.get('/collection').then(({ data }) => {
      setCollection(data);
      setLoading(false);
    });
  }, []);

  const toggle = (id: number) => {
    setSelected((prev) =>
      prev.includes(id)
        ? prev.filter((x) => x !== id)
        : prev.length < 3
        ? [...prev, id]
        : prev
    );
  };

  const handleCompare = async () => {
    if (selected.length < 2) return;
    setComparing(true);
    const { data } = await client.get('/fragrances/compare', { params: { ids: selected } });
    setCompared(data);
    setComparing(false);
  };

  const fields: Array<{ label: string; key: keyof Fragrance }> = [
    { label: 'Scent Family', key: 'scentFamily' },
    { label: 'Projection',   key: 'projection'  },
    { label: 'Longevity',    key: 'longevity'   },
    { label: 'Hot Season',   key: 'seasonHot'   },
    { label: 'Cold Season',  key: 'seasonCold'  },
    { label: 'Office Safe',  key: 'officeSafe'  },
  ];

  if (loading) return <Layout><p style={{ color: 'var(--ca-muted)' }}>Loading…</p></Layout>;

  return (
    <Layout>
      <h2 className="text-2xl font-semibold mb-2" style={{ color: 'var(--ca-gold)' }}>
        Compare Fragrances
      </h2>
      <p className="text-sm mb-6" style={{ color: 'var(--ca-muted)' }}>
        Select 2–3 fragrances from your collection.
      </p>

      <div className="flex flex-wrap gap-2 mb-6">
        {collection.map(({ fragrance }) => {
          const active = selected.includes(fragrance.id);
          return (
            <button
              key={fragrance.id}
              onClick={() => toggle(fragrance.id)}
              className="rounded-full px-3 py-1 text-sm border transition-all"
              style={active
                ? { background: 'var(--ca-gold)', color: '#0b0e1e', borderColor: 'var(--ca-gold)' }
                : { background: 'transparent', color: 'var(--ca-muted)', borderColor: 'var(--ca-border)' }}
            >
              {fragrance.brand} — {fragrance.name}
            </button>
          );
        })}
      </div>

      <button
        onClick={handleCompare}
        disabled={selected.length < 2 || comparing}
        className="mb-8 rounded px-6 py-2 text-sm font-semibold uppercase tracking-wider transition-opacity hover:opacity-80 disabled:opacity-40"
        style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
      >
        {comparing ? 'Comparing…' : 'Compare'}
      </button>

      {compared && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm border-collapse">
            <thead>
              <tr>
                <th className="text-left py-2 pr-4" style={{ color: 'var(--ca-muted)' }}></th>
                {compared.fragrances.map((f) => (
                  <th key={f.id} className="text-left py-2 px-4" style={{ color: 'var(--ca-gold)' }}>
                    <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.brand}</p>
                    {f.name}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {fields.map(({ label, key }) => (
                <tr key={key} className="border-t" style={{ borderColor: 'var(--ca-border)' }}>
                  <td className="py-3 pr-4 text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
                    {label}
                  </td>
                  {compared.fragrances.map((f) => (
                    <td key={f.id} className="py-3 px-4" style={{ color: 'var(--ca-text)' }}>
                      {typeof f[key] === 'boolean' ? (f[key] ? '✓' : '—') : String(f[key])}
                    </td>
                  ))}
                </tr>
              ))}
              <tr className="border-t" style={{ borderColor: 'var(--ca-border)' }}>
                <td className="py-3 pr-4 text-xs uppercase tracking-wider" style={{ color: 'var(--ca-muted)' }}>
                  Your Rating
                </td>
                {compared.fragrances.map((f) => (
                  <td key={f.id} className="py-3 px-4" style={{ color: 'var(--ca-gold)' }}>
                    {compared.ratings[f.id] ? '★'.repeat(compared.ratings[f.id]) : '—'}
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
