// ==================== CART API (backend thật) ====================
// Thay thế hoàn toàn cơ chế giỏ hàng localStorage cũ.
// Yêu cầu: navbar.js đã load trước (cung cấp fetchWithAuth, getAuthHeaders).

const CART_API = '/api/cart';

function isUserLoggedIn() {
    return !!(localStorage.getItem('displayName') || localStorage.getItem('accessToken'));
}

async function safeJson(res) {
    try {
        return await res.json();
    } catch (e) {
        return null;
    }
}

function formatVnd(value) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(value || 0)) + ' đ';
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.innerText = str ?? '';
    return div.innerHTML;
}

// ==================== CÁC HÀM GỌI API ====================

async function apiGetCart() {
    const res = await fetchWithAuth(CART_API);
    if (!res.ok) {
        const err = await safeJson(res);
        throw new Error(err?.message || 'Không tải được giỏ hàng');
    }
    return res.json();
}

async function apiAddToCart(foodId, quantity = 1, note = null) {
    const res = await fetchWithAuth(`${CART_API}/items`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ foodId, quantity, note })
    });
    if (!res.ok) {
        const err = await safeJson(res);
        throw new Error(err?.message || 'Thêm vào giỏ hàng thất bại');
    }
    return res.json();
}

async function apiUpdateCartItem(cartItemId, quantity, note) {
    const res = await fetchWithAuth(`${CART_API}/items/${cartItemId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity, note })
    });
    if (!res.ok) {
        const err = await safeJson(res);
        throw new Error(err?.message || 'Cập nhật giỏ hàng thất bại');
    }
    return res.json();
}

async function apiRemoveCartItem(cartItemId) {
    const res = await fetchWithAuth(`${CART_API}/items/${cartItemId}`, { method: 'DELETE' });
    if (!res.ok) {
        const err = await safeJson(res);
        throw new Error(err?.message || 'Xóa món thất bại');
    }
    return res.json();
}

async function apiClearCart() {
    const res = await fetchWithAuth(CART_API, { method: 'DELETE' });
    if (!res.ok) {
        const err = await safeJson(res);
        throw new Error(err?.message || 'Xóa giỏ hàng thất bại');
    }
    return res.json();
}

// Thêm món vào giỏ hàng thật + cập nhật preview trên navbar ngay lập tức
async function addFoodToCart(foodId, quantity = 1, note = null) {
    if (!isUserLoggedIn()) {
        alert('Vui lòng đăng nhập để đặt món!');
        sessionStorage.setItem('redirectAfterLogin', window.location.href);
        window.location.href = '/login';
        return null;
    }
    const cart = await apiAddToCart(foodId, quantity, note);
    renderNavbarCart(cart);
    window.dispatchEvent(new CustomEvent('cartUpdated', { detail: cart }));
    return cart;
}

window.CART_API = CART_API;
window.formatVnd = formatVnd;
window.escapeHtml = escapeHtml;
window.apiGetCart = apiGetCart;
window.apiAddToCart = apiAddToCart;
window.apiUpdateCartItem = apiUpdateCartItem;
window.apiRemoveCartItem = apiRemoveCartItem;
window.apiClearCart = apiClearCart;
window.addFoodToCart = addFoodToCart;
window.isUserLoggedIn = isUserLoggedIn;

// ==================== PREVIEW GIỎ HÀNG TRÊN NAVBAR ====================

function renderNavbarCart(cart) {
    const countEl = document.getElementById('navbarCartCount');
    const itemsEl = document.getElementById('navbarCartItems');
    const totalEl = document.getElementById('navbarCartTotal');
    const checkoutBtn = document.getElementById('btnNavbarCheckout');

    if (!countEl) return; // trang này không có navbar giỏ hàng

    const allItems = (cart.merchantGroups || [])
        .flatMap(g => (g.items || []).map(it => ({ ...it, merchantName: g.merchantName })));

    const totalQty = allItems.reduce((sum, it) => sum + it.quantity, 0);

    countEl.innerText = String(totalQty);

    if (itemsEl) {
        if (allItems.length === 0) {
            itemsEl.innerHTML = `
                <div class="text-center py-4 text-muted fs-8">
                    <i class="bi bi-basket fs-2 d-block opacity-50 mb-1"></i>
                    <span>Giỏ hàng trống</span>
                </div>`;
        } else {
            const shown = allItems.slice(0, 6);
            itemsEl.innerHTML = shown.map(it => `
                <div class="d-flex align-items-center justify-content-between mb-2 pb-2 border-bottom border-light-subtle fs-8">
                    <div class="pe-2 overflow-hidden flex-grow-1" style="max-width:190px;">
                        <span class="d-block fw-semibold text-dark text-truncate" title="${escapeHtml(it.foodName)}">${escapeHtml(it.foodName)}</span>
                        <span class="text-muted">x${it.quantity} · ${escapeHtml(it.merchantName)}</span>
                    </div>
                    <span class="text-danger fw-bold flex-shrink-0">${formatVnd(it.subtotal)}</span>
                </div>
            `).join('');

            if (allItems.length > shown.length) {
                itemsEl.innerHTML += `<div class="text-center text-muted fs-8 pt-1">và ${allItems.length - shown.length} món khác...</div>`;
            }
        }
    }

    if (totalEl) totalEl.innerText = formatVnd(cart.totalAmount);

    if (checkoutBtn) {
        if (totalQty === 0) {
            checkoutBtn.setAttribute('disabled', 'true');
            checkoutBtn.classList.add('opacity-50');
        } else {
            checkoutBtn.removeAttribute('disabled');
            checkoutBtn.classList.remove('opacity-50');
        }
    }
}

window.renderNavbarCart = renderNavbarCart;

async function refreshNavbarCart() {
    const countEl = document.getElementById('navbarCartCount');
    if (!countEl) return;

    if (!isUserLoggedIn()) {
        countEl.innerText = '0';
        return;
    }

    try {
        const cart = await apiGetCart();
        renderNavbarCart(cart);
    } catch (e) {
        console.warn('Không tải được giỏ hàng cho navbar:', e.message);
    }
}

window.refreshNavbarCart = refreshNavbarCart;

document.addEventListener('DOMContentLoaded', () => {
    refreshNavbarCart();

    document.getElementById('btnNavbarCheckout')?.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (document.getElementById('btnNavbarCheckout')?.hasAttribute('disabled')) return;
        window.location.href = '/cart';
    });
});

window.addEventListener('cartUpdated', (e) => {
    if (e.detail) {
        renderNavbarCart(e.detail);
    } else {
        refreshNavbarCart();
    }
});
