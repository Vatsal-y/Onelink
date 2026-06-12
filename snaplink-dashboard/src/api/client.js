const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * API client with automatic JWT token injection.
 */
class ApiClient {
  constructor() {
    this.baseUrl = API_BASE;
  }

  getToken() {
    return localStorage.getItem('snaplink_token');
  }

  async request(path, options = {}) {
    const url = `${this.baseUrl}${path}`;
    const token = this.getToken();

    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      localStorage.removeItem('snaplink_token');
      window.location.href = '/login';
      throw new Error('Session expired. Please log in again.');
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || `Request failed with status ${response.status}`);
    }

    return data;
  }

  // ---- Auth ----
  async register(email, password) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  }

  async login(email, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  }

  // ---- Links ----
  async shortenUrl(longUrl, alias, expiresIn) {
    return this.request('/api/shorten', {
      method: 'POST',
      body: JSON.stringify({ longUrl, alias: alias || undefined, expiresIn: expiresIn || 'never' }),
    });
  }

  async listLinks() {
    return this.request('/api/links');
  }

  async deleteLink(code) {
    return this.request(`/api/links/${code}`, { method: 'DELETE' });
  }

  async getQrCode(code) {
    return this.request(`/api/links/${code}/qr`);
  }

  // ---- Analytics ----
  async getAnalytics(code, days = 7) {
    return this.request(`/api/analytics/${code}?days=${days}`);
  }
}

const api = new ApiClient();
export default api;
