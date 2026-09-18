/**
 * Thin fetch wrapper. Every request that needs auth automatically gets the stored JWT; every
 * response is parsed as JSON (or returns null for 204s); every failure is normalized into an Error
 * whose .message is already the user-facing text from the backend's ErrorResponse, and whose
 * .validationErrors (when present) lets form code highlight individual fields.
 */
const api = (() => {
  const cfg = window.YAXINAZ_CONFIG;

  function getToken() {
    return localStorage.getItem(cfg.tokenStorageKey);
  }

  function buildUrl(path, query) {
    const url = path.startsWith('http') ? path : cfg.apiBaseUrl + path;
    if (!query) return url;
    const params = new URLSearchParams();
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') params.set(key, value);
    });
    const qs = params.toString();
    return qs ? `${url}?${qs}` : url;
  }

  async function request(method, path, { body, query, skipAuth } = {}) {
    const headers = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (!skipAuth) {
      const token = getToken();
      if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    let response;
    try {
      response = await fetch(buildUrl(path, query), {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
      });
    } catch (networkError) {
      throw new ApiError(translate('Unable to reach the server. Please check your connection.'), 0, null);
    }

    if (response.status === 401 && !skipAuth) {
      localStorage.removeItem(cfg.tokenStorageKey);
      localStorage.removeItem(cfg.userStorageKey);
      if (!location.pathname.endsWith('login.html')) {
        const returnTo = encodeURIComponent(location.pathname + location.search);
        location.href = `/login.html?returnTo=${returnTo}&expired=1`;
      }
      throw new ApiError(translate('Your session has expired. Please sign in again.'), 401, null);
    }

    if (response.status === 204) return null;

    let payload = null;
    const text = await response.text();
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch (e) {
        payload = null;
      }
    }

    if (!response.ok) {
      const message = translate((payload && payload.message) || defaultMessageFor(response.status));
      throw new ApiError(message, response.status, payload);
    }

    return payload;
  }

  /**
   * The backend has no locale support, so ErrorResponse.message (and these client-authored
   * fallbacks) are always plain English. translateApiMessage() maps the finite set of known exact
   * strings this app actually produces to the current UI language; anything else (e.g. a dynamic
   * "Issue with id 42 not found" message) is shown as-is - translating arbitrary backend prose is
   * out of scope without server-side i18n.
   */
  function translate(message) {
    return (typeof i18n !== 'undefined' && i18n.translateApiMessage) ? i18n.translateApiMessage(message) : message;
  }

  function defaultMessageFor(status) {
    if (status === 403) return 'You do not have permission to perform this action.';
    if (status === 404) return 'The requested item could not be found.';
    if (status === 409) return 'This record was updated by another user. Please refresh and try again.';
    if (status === 429) return 'You are doing that too often. Please wait a moment and try again.';
    if (status >= 500) return 'Something went wrong on our end. Please try again shortly.';
    return 'The request could not be completed.';
  }

  /**
   * Uploads a file to POST /api/files and returns { url, filename, sizeBytes, contentType }. Uses
   * FormData directly rather than request() - a multipart body must NOT get the JSON
   * Content-Type header request() always sets, and the browser needs to set its own
   * multipart boundary, which only happens when no Content-Type is set manually.
   */
  async function uploadFile(file) {
    const headers = {};
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const formData = new FormData();
    formData.append('file', file);
    let response;
    try {
      response = await fetch(buildUrl('/files'), { method: 'POST', headers, body: formData });
    } catch (networkError) {
      throw new ApiError(translate('Unable to reach the server. Please check your connection.'), 0, null);
    }
    const text = await response.text();
    let payload = null;
    try { payload = text ? JSON.parse(text) : null; } catch (e) { payload = null; }
    if (!response.ok) {
      throw new ApiError(translate((payload && payload.message) || defaultMessageFor(response.status)), response.status, payload);
    }
    return payload;
  }

  return {
    get: (path, opts) => request('GET', path, opts),
    post: (path, body, opts) => request('POST', path, { ...opts, body }),
    patch: (path, body, opts) => request('PATCH', path, { ...opts, body }),
    put: (path, body, opts) => request('PUT', path, { ...opts, body }),
    delete: (path, opts) => request('DELETE', path, opts),
    uploadFile,
    getToken,
  };
})();

class ApiError extends Error {
  constructor(message, status, payload) {
    super(message);
    this.status = status;
    this.validationErrors = payload && payload.validationErrors;
  }
}
