const auth = (() => {
  const cfg = window.YAXINAZ_CONFIG;

  function saveSession(authResponse) {
    localStorage.setItem(cfg.tokenStorageKey, authResponse.token);
    localStorage.setItem(cfg.userStorageKey, JSON.stringify(authResponse.user));
  }

  async function login(email, password) {
    const response = await api.post('/auth/login', { email, password }, { skipAuth: true });
    saveSession(response);
    return response.user;
  }

  async function register(payload) {
    const response = await api.post('/auth/register', payload, { skipAuth: true });
    saveSession(response);
    return response.user;
  }

  function logout() {
    localStorage.removeItem(cfg.tokenStorageKey);
    localStorage.removeItem(cfg.userStorageKey);
    localStorage.removeItem(cfg.communityStorageKey);
    location.href = '/login.html';
  }

  function currentUser() {
    const raw = localStorage.getItem(cfg.userStorageKey);
    return raw ? JSON.parse(raw) : null;
  }

  function isAuthenticated() {
    return !!api.getToken();
  }

  /** Call at the top of every protected page. Redirects to login if not signed in. */
  function requireAuth() {
    if (!isAuthenticated()) {
      const returnTo = encodeURIComponent(location.pathname + location.search);
      location.href = `/login.html?returnTo=${returnTo}`;
      throw new Error('redirecting');
    }
    return currentUser();
  }

  /**
   * Client-side role gate - cosmetic only (hides admin nav items for residents). The backend is
   * the real authorization boundary; never rely on this for anything security-sensitive.
   */
  function hasAnyRole(...roles) {
    const user = currentUser();
    return !!user && roles.includes(user.role);
  }

  async function refreshCurrentUser() {
    const user = await api.get('/auth/me');
    localStorage.setItem(cfg.userStorageKey, JSON.stringify(user));
    return user;
  }

  return { login, register, logout, currentUser, isAuthenticated, requireAuth, hasAnyRole, refreshCurrentUser };
})();
