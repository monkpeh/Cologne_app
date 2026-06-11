import { useEffect, useState } from 'react'; import type { FormEvent } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { BrowseResponse } from '../types';

export default function Browse() {
  const [data, setData]         = useState<BrowseResponse | null>(null);
  const [query, setQuery]       = useState('');
  const [loading, setLoading]   = useState(true);
  const [showForm, setShowForm] = useState(false);

  const load = async (q = '') => {
    setLoading(true);
    const { data: res } = await client.get<BrowseResponse>('/fragrances', { params: { q } });
    setData(res);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    load(query);
  };

  const handleAdd = async (id: number) => {
    await client.post(`/collection/${id}`);
    setData((prev) => prev ? { ...prev, collectionIds: [...prev.collectionIds, id] } : prev);
  };

  const handleRemove = async (id: number) => {
    await client.delete(`/collection/${id}`);
    setData((prev) => prev ? { ...prev, collectionIds: prev.collectionIds.filter((x) => x !== id) } : prev);
  };

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-semibold" style={{ color: 'var(--ca-gold)' }}>
          Browse Catalogue
        </h2>
        <button
          onClick={() => setShowForm((v) => !v)}
          className="px-4 py-2 rounded text-sm font-semibold transition-opacity hover:opacity-80"
          style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
        >
          {showForm ? 'Close' : '+ Submit Fragrance'}
        </button>
      </div>

      {showForm && <SubmitForm onSuccess={() => { setShowForm(false); load(query); }} />}

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search brand, name, scent family…"
          className="flex-1 rounded px-3 py-2 text-sm outline-none"
          style={{
            background: 'var(--ca-surface)',
            color: 'var(--ca-text)',
            border: '1px solid var(--ca-border)',
          }}
        />
        <button
          type="submit"
          className="px-4 py-2 rounded text-sm font-semibold transition-opacity hover:opacity-80"
          style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
        >
          Search
        </button>
      </form>

      {loading ? (
        <p style={{ color: 'var(--ca-muted)' }}>Loading…</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data?.fragrances.map((f) => {
            const inCollection = data.collectionIds.includes(f.id);
            const avg = data.averageRatings[f.id];
            return (
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
                      style={{ background: 'var(--ca-surface-2)', color: 'var(--ca-muted)' }}>{tag}</span>
                  ))}
                  {f.officeSafe && (
                    <span className="rounded-full px-2 py-0.5"
                      style={{ background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}>Office Safe</span>
                  )}
                </div>
                {avg != null && (
                  <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>
                    Avg rating: <span style={{ color: 'var(--ca-gold)' }}>{'★'.repeat(Math.round(avg))}</span> ({avg.toFixed(1)})
                  </p>
                )}
                <button
                  onClick={() => inCollection ? handleRemove(f.id) : handleAdd(f.id)}
                  className="rounded px-3 py-1.5 text-xs font-semibold transition-opacity hover:opacity-80"
                  style={inCollection
                    ? { background: 'rgba(248,113,113,.1)', color: '#f87171' }
                    : { background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}
                >
                  {inCollection ? 'Remove from Collection' : 'Add to Collection'}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </Layout>
  );
}

function SubmitForm({ onSuccess }: { onSuccess: () => void }) {
  const empty = {
    brand: '', name: '', scentFamily: '', projection: 'Medium', longevity: 'Moderate',
    seasonHot: false, seasonCold: false, officeSafe: false, description: '', imageUrl: '',
  };
  const [form, setForm] = useState(empty);
  const [err, setErr]   = useState('');

  const set = (k: string, v: any) => setForm((prev) => ({ ...prev, [k]: v }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErr('');
    try {
      await client.post('/fragrances', form);
      onSuccess();
    } catch (ex: any) {
      setErr(ex.response?.data?.error ?? 'Submission failed.');
    }
  };

  const inputClass = "rounded px-3 py-2 text-sm outline-none w-full";
  const inputStyle = { background: 'var(--ca-surface-2)', color: 'var(--ca-text)', border: '1px solid var(--ca-border)' };
  const labelStyle = { color: 'var(--ca-muted)' };

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-xl border p-6 mb-6 grid gap-4 sm:grid-cols-2"
      style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
    >
      <h3 className="sm:col-span-2 text-lg font-semibold" style={{ color: 'var(--ca-gold)' }}>
        Submit a Fragrance
      </h3>
      {[['Brand', 'brand'], ['Name', 'name'], ['Scent Family', 'scentFamily'], ['Image URL', 'imageUrl']].map(([label, key]) => (
        <div key={key} className="flex flex-col gap-1">
          <label className="text-xs uppercase tracking-wider" style={labelStyle}>{label}</label>
          <input className={inputClass} style={inputStyle} value={(form as any)[key]}
            onChange={(e) => set(key, e.target.value)} required={key !== 'imageUrl'} />
        </div>
      ))}
      <div className="flex flex-col gap-1">
        <label className="text-xs uppercase tracking-wider" style={labelStyle}>Projection</label>
        <select className={inputClass} style={inputStyle} value={form.projection} onChange={(e) => set('projection', e.target.value)}>
          {['Intimate', 'Soft', 'Medium', 'Strong', 'Beast Mode'].map((o) => <option key={o}>{o}</option>)}
        </select>
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-xs uppercase tracking-wider" style={labelStyle}>Longevity</label>
        <select className={inputClass} style={inputStyle} value={form.longevity} onChange={(e) => set('longevity', e.target.value)}>
          {['Poor', 'Moderate', 'Good', 'Excellent', 'Eternal'].map((o) => <option key={o}>{o}</option>)}
        </select>
      </div>
      <div className="sm:col-span-2 flex flex-col gap-1">
        <label className="text-xs uppercase tracking-wider" style={labelStyle}>Description</label>
        <textarea className={inputClass} style={inputStyle} rows={3} value={form.description}
          onChange={(e) => set('description', e.target.value)} />
      </div>
      <div className="sm:col-span-2 flex gap-6">
        {[['seasonHot', 'Hot season'], ['seasonCold', 'Cold season'], ['officeSafe', 'Office safe']].map(([key, label]) => (
          <label key={key} className="flex items-center gap-2 text-sm cursor-pointer" style={{ color: 'var(--ca-text)' }}>
            <input type="checkbox" checked={(form as any)[key]} onChange={(e) => set(key, e.target.checked)} />
            {label}
          </label>
        ))}
      </div>
      {err && <p className="sm:col-span-2 text-sm" style={{ color: '#f87171' }}>{err}</p>}
      <button type="submit"
        className="sm:col-span-2 rounded py-2 text-sm font-semibold uppercase tracking-wider transition-opacity hover:opacity-80"
        style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}>
        Submit
      </button>
    </form>
  );
}
