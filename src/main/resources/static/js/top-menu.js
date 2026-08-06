(() => {
    function getCartCount() {
        for (const key of ['mealChoiceCart', 'cart']) {
            try {
                const value = JSON.parse(localStorage.getItem(key) || '[]');
                if (Array.isArray(value)) {
                    return value.reduce((total, item) => total + Number(item.quantity || 1), 0);
                }
            } catch (error) {
                // Bỏ qua dữ liệu giỏ hàng cũ không đúng định dạng.
            }
        }
        return 0;
    }

    async function hydrateMenu() {
        const cartBadge = document.querySelector('a[href="/cart"] .badge');
        if (cartBadge) cartBadge.textContent = String(getCartCount());

        const token = localStorage.getItem('accessToken');
        if (!token) return;
        try {
            const response = await fetch('/api/user/profile', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) return;
            const result = await response.json();
            const accountText = document.querySelector('.nav-item.dropdown .btn-light .fw-semibold');
            if (accountText && result.data?.displayName) {
                accountText.textContent = result.data.displayName;
            }
        } catch (error) {
            console.debug('Không thể tải thông tin menu tài khoản', error);
        }
    }

    window.handleLogout = async function handleLogout(event) {
        event?.preventDefault();
        if (!confirm('Bạn có chắc chắn muốn đăng xuất không?')) return;
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('accessToken') || ''}`
                    },
                    body: JSON.stringify({ refreshToken })
                });
            }
        } finally {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
        }
    };

    document.addEventListener('DOMContentLoaded', hydrateMenu);
})();
