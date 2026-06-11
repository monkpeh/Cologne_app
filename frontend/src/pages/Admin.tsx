import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import client from '../api/client';
import type { AppUser, Fragrance } from '../types';
import { useAuthStore } from '../store/authStore';

type Tab = 'users' | 'fragrances';

export default function Admin() {
  const [tab, setTab] = useState<Tab>('users');

  const tabStyle = (t: Tab) => ({
    padding: '8px 20px',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: 600,
    cursor: 'pointer',
    border: 'none',
    background: tab === t ? 'var(--ca-gold)' : 'transparent',
    color: tab === t ? '#0b0e1e' : 'var(--ca-muted)',
  });

  return (
    <Layout>
      <h2 className="text-2xl font-semibold mb-6" style={{ color: 'var(--ca-gold)' }}>
        Admin Panel
      </h2>
      <div className="flex gap-2 mb-6">
        <button style={tabStyle('users')}     onClick={() => setTab('users')}>Users</button>
        <button style={tabStyle('fragrances')} onClick={() => setTab('fragrances')}>Fragrances</button>
      </div>
      {tab === 'users'      && <UsersTab />}
      {tab === 'fragrances' && <FragrancesTab />}
    </Layout>
  );
}

// ── Users tab ────────────────────────────────────────────────────────────────

function UsersTab() {
  const [users, setUsers]         = useState<AppUser[]>([]);
  const [newPw, setNewPw]         = useState<Record<number, string>>({});
  const { username: self }        = useAuthStore();

  useEffect(() => {
    client.get('/admin/users').then(({ data }) => setUsers(data));
  }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this user?')) return;
    await client.delete(`/admin/users/${id}`);
    setUsers((prev) => prev.filter((u) => u.id !== id));
  };

  const handleReset = async (id: number) => {
    const pw = newPw[id];
    if (!pw || pw.length < 6) { alert('Password must be at least 6 characters.'); return; }
    await client.post(`/admin/users/${id}/reset-password`, { newPassword: pw });
    setNewPw((prev) => ({ ...prev, [id]: '' }));
    alert('Password reset.');
  };

  const handleToggleRole = async (id: number) => {
    const { data } = await client.post(`/admin/users/${id}/toggle-role`);
    setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, role: data.newRole } : u)));
  };

  const inputStyle = {
    background: 'var(--ca-surface-2)',
    color: 'var(--ca-text)',
    border: '1px solid var(--ca-border)',
    borderRadius: '4px',
    padding: '4px 8px',
    fontSize: '12px',
    outline: 'none',
  };

  return (
    <div className="flex flex-col gap-3">
      {users.map((u) => (
        <div
          key={u.id}
          className="rounded-xl border p-4 flex flex-wrap gap-3 items-center"
          style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
        >
          <div className="flex-1 min-w-[120px]">
            <p className="font-semibold text-sm" style={{ color: 'var(--ca-text)' }}>{u.username}</p>
            <p className="text-xs" style={{ color: u.role === 'ADMIN' ? 'var(--ca-gold)' : 'var(--ca-muted)' }}>
              {u.role}
            </p>
          </div>

          <div className="flex gap-2 items-center flex-wrap">
            <input
              type="password"
              placeholder="New password"
              style={inputStyle}
              value={newPw[u.id] ?? ''}
              onChange={(e) => setNewPw((prev) => ({ ...prev, [u.id]: e.target.value }))}
            />
            <button
              onClick={() => handleReset(u.id)}
              className="text-xs rounded px-2 py-1 transition-opacity hover:opacity-80"
              style={{ background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}
            >
              Reset PW
            </button>

            {u.username !== self && (
              <>
                <button
                  onClick={() => handleToggleRole(u.id)}
                  className="text-xs rounded px-2 py-1 transition-opacity hover:opacity-80"
                  style={{ background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}
                >
                  Toggle Role
                </button>
                <button
                  onClick={() => handleDelete(u.id)}
                  className="text-xs rounded px-2 py-1 transition-opacity hover:opacity-80"
                  style={{ background: 'rgba(248,113,113,.1)', color: '#f87171' }}
                >
                  Delete
                </button>
              </>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

// ── Fragrances tab ───────────────────────────────────────────────────────────

const emptyForm = {
  brand: '', name: '', scentFamily: '', projection: 'Medium', longevity: 'Moderate',
  seasonHot: false, seasonCold: false, officeSafe: false, description: '', imageUrl: '',
};

function FragrancesTab() {
  const [fragrances, setFragrances] = useState<Fragrance[]>([]);
  const [editing, setEditing]       = useState<number | null>(null);
  const [form, setForm]             = useState(emptyForm);
  const [showAdd, setShowAdd]       = useState(false);
  const [addForm, setAddForm]       = useState(emptyForm);

  useEffect(() => {
    client.get('/admin/fragrances').then(({ data }) => setFragrances(data));
  }, []);

  const startEdit = (f: Fragrance) => {
    setEditing(f.id);
    setForm({
      brand: f.brand, name: f.name, scentFamily: f.scentFamily,
      projection: f.projection, longevity: f.longevity,
      seasonHot: f.seasonHot, seasonCold: f.seasonCold,
      officeSafe: f.officeSafe, description: f.description,
      imageUrl: f.imageUrl ?? '',
    });
  };

  const handleUpdate = async (id: number) => {
    const { data } = await client.put(`/admin/fragrances/${id}`, form);
    setFragrances((prev) => prev.map((f) => (f.id === id ? data : f)));
    setEditing(null);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this fragrance?')) return;
    await client.delete(`/admin/fragrances/${id}`);
    setFragrances((prev) => prev.filter((f) => f.id !== id));
  };

  const handleAdd = async () => {
    const { data } = await client.post('/admin/fragrances', addForm);
    setFragrances((prev) => [...prev, data]);
    setAddForm(emptyForm);
    setShowAdd(false);
  };

  const inputStyle = {
    background: 'var(--ca-surface-2)',
    color: 'var(--ca-text)',
    border: '1px solid var(--ca-border)',
    borderRadius: '4px',
    padding: '4px 8px',
    fontSize: '12px',
    outline: 'none',
    width: '100%',
  };

  return (
    <div>
      <div className="flex justify-end mb-4">
        <button
          onClick={() => setShowAdd((v) => !v)}
          className="rounded px-4 py-2 text-sm font-semibold transition-opacity hover:opacity-80"
          style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
        >
          {showAdd ? 'Cancel' : '+ Add Fragrance'}
        </button>
      </div>

      {showAdd && (
        <FragranceFormCard
          form={addForm}
          setForm={setAddForm}
          onSubmit={handleAdd}
          inputStyle={inputStyle}
          submitLabel="Add"
          onCancel={() => setShowAdd(false)}
        />
      )}

      <div className="flex flex-col gap-3">
        {fragrances.map((f) =>
          editing === f.id ? (
            <FragranceFormCard
              key={f.id}
              form={form}
              setForm={setForm}
              onSubmit={() => handleUpdate(f.id)}
              inputStyle={inputStyle}
              submitLabel="Save"
              onCancel={() => setEditing(null)}
            />
          ) : (
            <div
              key={f.id}
              className="rounded-xl border p-4 flex justify-between items-center gap-3"
              style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
            >
              <div>
                <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.brand}</p>
                <p className="font-semibold text-sm" style={{ color: 'var(--ca-text)' }}>{f.name}</p>
                <p className="text-xs" style={{ color: 'var(--ca-muted)' }}>{f.scentFamily}</p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => startEdit(f)}
                  className="text-xs rounded px-2 py-1 transition-opacity hover:opacity-80"
                  style={{ background: 'rgba(201,162,85,.12)', color: 'var(--ca-gold)' }}
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(f.id)}
                  className="text-xs rounded px-2 py-1 transition-opacity hover:opacity-80"
                  style={{ background: 'rgba(248,113,113,.1)', color: '#f87171' }}
                >
                  Delete
                </button>
              </div>
            </div>
          )
        )}
      </div>
    </div>
  );
}

function FragranceFormCard({
  form, setForm, onSubmit, inputStyle, submitLabel, onCancel,
}: {
  form: typeof emptyForm;
  setForm: (f: typeof emptyForm) => void;
  onSubmit: () => void;
  inputStyle: React.CSSProperties;
  submitLabel: string;
  onCancel: () => void;
}) {
  const set = (k: string, v: any) => setForm({ ...form, [k]: v });

  return (
    <div
      className="rounded-xl border p-5 mb-3 grid gap-3 sm:grid-cols-2"
      style={{ background: 'var(--ca-surface)', borderColor: 'var(--ca-border)' }}
    >
      {[['Brand', 'brand'], ['Name', 'name'], ['Scent Family', 'scentFamily'], ['Image URL', 'imageUrl']].map(([label, key]) => (
        <div key={key}>
          <p className="text-xs mb-1" style={{ color: 'var(--ca-muted)' }}>{label}</p>
          <input style={inputStyle} value={(form as any)[key]} onChange={(e) => set(key, e.target.value)} />
        </div>
      ))}
      <div>
        <p className="text-xs mb-1" style={{ color: 'var(--ca-muted)' }}>Projection</p>
        <select style={inputStyle} value={form.projection} onChange={(e) => set('projection', e.target.value)}>
          {['Intimate', 'Soft', 'Medium', 'Strong', 'Beast Mode'].map((o) => <option key={o}>{o}</option>)}
        </select>
      </div>
      <div>
        <p className="text-xs mb-1" style={{ color: 'var(--ca-muted)' }}>Longevity</p>
        <select style={inputStyle} value={form.longevity} onChange={(e) => set('longevity', e.target.value)}>
          {['Poor', 'Moderate', 'Good', 'Excellent', 'Eternal'].map((o) => <option key={o}>{o}</option>)}
        </select>
      </div>
      <div className="sm:col-span-2">
        <p className="text-xs mb-1" style={{ color: 'var(--ca-muted)' }}>Description</p>
        <textarea style={{ ...inputStyle, height: '60px' }} value={form.description}
          onChange={(e) => set('description', e.target.value)} />
      </div>
      <div className="sm:col-span-2 flex gap-6">
        {[['seasonHot', 'Hot Season'], ['seasonCold', 'Cold Season'], ['officeSafe', 'Office Safe']].map(([key, label]) => (
          <label key={key} className="flex items-center gap-2 text-xs cursor-pointer" style={{ color: 'var(--ca-text)' }}>
            <input type="checkbox" checked={(form as any)[key]} onChange={(e) => set(key, e.target.checked)} />
            {label}
          </label>
        ))}
      </div>
      <div className="sm:col-span-2 flex gap-2">
        <button
          onClick={onSubmit}
          className="rounded px-4 py-1.5 text-sm font-semibold transition-opacity hover:opacity-80"
          style={{ background: 'var(--ca-gold)', color: '#0b0e1e' }}
        >
          {submitLabel}
        </button>
        <button
          onClick={onCancel}
          className="rounded px-4 py-1.5 text-sm transition-opacity hover:opacity-80"
          style={{ background: 'transparent', color: 'var(--ca-muted)', border: '1px solid var(--ca-border)' }}
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
