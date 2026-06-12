import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import CreateLinkPage from './pages/CreateLinkPage.jsx';
import AnalyticsPage from './pages/AnalyticsPage.jsx';

export default function App() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-page">
        <div className="spinner" style={{ width: 40, height: 40 }} />
      </div>
    );
  }

  return (
    <>
      <Navbar />
      <main className="page">
        <div className="container">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={
              <ProtectedRoute><DashboardPage /></ProtectedRoute>
            } />
            <Route path="/create" element={
              <ProtectedRoute><CreateLinkPage /></ProtectedRoute>
            } />
            <Route path="/analytics/:code" element={
              <ProtectedRoute><AnalyticsPage /></ProtectedRoute>
            } />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </main>
    </>
  );
}
