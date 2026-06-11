import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Login      from './pages/Login';
import Register   from './pages/Register';
import Collection from './pages/Collection';
import Browse     from './pages/Browse';
import Recommend  from './pages/Recommend';
import Stats      from './pages/Stats';
import Compare    from './pages/Compare';
import Admin      from './pages/Admin';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"    element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/collection" element={<ProtectedRoute><Collection /></ProtectedRoute>} />
        <Route path="/browse"     element={<ProtectedRoute><Browse /></ProtectedRoute>} />
        <Route path="/recommend"  element={<ProtectedRoute><Recommend /></ProtectedRoute>} />
        <Route path="/stats"      element={<ProtectedRoute><Stats /></ProtectedRoute>} />
        <Route path="/compare"    element={<ProtectedRoute><Compare /></ProtectedRoute>} />
        <Route path="/admin"      element={<ProtectedRoute requireAdmin><Admin /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/collection" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
