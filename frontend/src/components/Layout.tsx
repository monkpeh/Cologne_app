import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const navItems = [
  { to: '/collection', label: 'Collection' },
  { to: '/browse',     label: 'Browse' },
  { to: '/recommend',  label: 'Recommend' },
  { to: '/stats',      label: 'Stats' },
  { to: '/compare',    label: 'Compare' },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const { username, isAdmin, logout } = useAuthStore();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ background: 'var(--ca-bg)' }}>
      {/* ── Top nav ─────────────────────────────────────────────────────── */}
      <nav
        className="sticky top-0 z-50 flex items-center justify-between px-6 py-3 border-b"
        style={{
          background: 'var(--ca-surface)',
          borderColor: 'var(--ca-border)',
        }}
      >
        <Link
          to="/collection"
          className="text-lg font-semibold tracking-widest uppercase"
          style={{ color: 'var(--ca-gold)' }}
        >
          Cologne Advisor
        </Link>

        <div className="flex items-center gap-1">
          {navItems.map(({ to, label }) => (
            <Link
              key={to}
              to={to}
              className="px-3 py-1.5 rounded text-sm transition-colors"
              style={{
                color: pathname === to ? 'var(--ca-gold)' : 'var(--ca-muted)',
                background: pathname === to ? 'rgba(201,162,85,.1)' : 'transparent',
              }}
            >
              {label}
            </Link>
          ))}
          {isAdmin() && (
            <Link
              to="/admin"
              className="px-3 py-1.5 rounded text-sm transition-colors"
              style={{
                color: pathname.startsWith('/admin') ? 'var(--ca-gold)' : 'var(--ca-muted)',
                background: pathname.startsWith('/admin') ? 'rgba(201,162,85,.1)' : 'transparent',
              }}
            >
              Admin
            </Link>
          )}
        </div>

        <div className="flex items-center gap-3">
          <span className="text-sm" style={{ color: 'var(--ca-muted)' }}>
            {username}
          </span>
          <button
            onClick={handleLogout}
            className="px-3 py-1.5 rounded text-sm border transition-colors hover:opacity-80"
            style={{
              borderColor: 'var(--ca-border)',
              color: 'var(--ca-muted)',
            }}
          >
            Sign out
          </button>
        </div>
      </nav>

      {/* ── Page content ────────────────────────────────────────────────── */}
      <main className="flex-1 px-6 py-8 max-w-6xl mx-auto w-full">{children}</main>
    </div>
  );
}
