const host = location.hostname === 'localhost' && location.port === '63342'
    ? 'localhost:8080'
    : location.host;
window.API_BASE_URL = `${location.protocol}//${host}/api`;

function getJwtToken() {
    return localStorage.getItem('jwtToken');
}

function authHeaders() {
    const token = getJwtToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return headers;
}

// --- <ajoutez ceci> ---
;(function() {
    const _fetch = window.fetch;
    window.fetch = (input, init = {}) => {
        // On n’applique l’en-tête JWT que si on appelle l’API
        const url = typeof input === 'string' ? input : input.url;
        if (url.startsWith(window.API_BASE_URL)) {
            init.headers = {
                // fusionne vos éventuels headers passés manuellement…
                ...(init.headers || {}),
                // …avec l’Authorization (ou seulement Content-Type si pas de token)
                ...authHeaders()
            };
        }
        return _fetch(input, init);
    };
})();

async function apiFetch(path, options = {}) {
    const url = `${window.API_BASE_URL}${path}`;
    const opts = {
        ...options,
        headers: {
            // prend d’abord vos headers passés à options…
            ...(options.headers || {}),
            // …puis ajoute Content-Type + Bearer si existant
            ...authHeaders()
        }
    };
    const response = await fetch(url, opts);
    if (!response.ok) {
        const err = new Error(`HTTP ${response.status}`);
        err.response = response;
        throw err;
    }
    return response.json();
}

// Exportez-le globalement
window.apiFetch = apiFetch;