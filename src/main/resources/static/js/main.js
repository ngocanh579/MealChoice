window.MealChoice = (() => {
    function authHeaders() {
        const token = localStorage.getItem('accessToken');
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        };
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...authHeaders(),
                ...(options.headers || {})
            }
        });

        let payload = {};
        try {
            payload = await response.json();
        } catch (error) {
            payload = { message: response.statusText || 'Máy chủ không trả về dữ liệu hợp lệ' };
        }

        if (!response.ok) {
            const requestError = new Error(payload.message || 'Yêu cầu không thành công');
            requestError.status = response.status;
            throw requestError;
        }
        return payload;
    }

    function requireLogin() {
        if (!localStorage.getItem('accessToken')) {
            window.location.href = '/login';
            return false;
        }
        return true;
    }

    function alert(element, type, message) {
        if (!element) return;
        element.className = `mc-alert show mc-alert-${type}`;
        element.textContent = message;
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    return { authHeaders, request, requireLogin, alert, escapeHtml };
})();
