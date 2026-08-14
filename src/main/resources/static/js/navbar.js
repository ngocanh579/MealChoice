// Xử lý đăng xuất (Scope toàn cục để html onclick gọi trực tiếp)
async function handleLogout(e) {
    if (e) e.preventDefault();

    if (confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken: refreshToken || '' })
            });
        } catch (error) {
            console.error("Lỗi đăng xuất:", error);
        } finally {
            document.cookie = "accessToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
            localStorage.clear();
            sessionStorage.clear();
            window.location.href = "/login";
        }
    }
}

// Logic khởi tạo và đồng bộ trạng thái hiển thị Navbar
function initNavbar() {
    const displayName = localStorage.getItem('displayName');
    const accessToken = localStorage.getItem('accessToken');

    const guestNav = document.getElementById('guestNav');
    const userProfileNav = document.getElementById('userProfileNav');
    const registerMerchantNav = document.getElementById('registerMerchantNav');
    const merchantPendingNav = document.getElementById('merchantPendingNav');
    const merchantRoleNav = document.getElementById('merchantRoleNav');
    const adminRoleNav = document.getElementById('adminRoleNav');

    // 1. Hiển thị User hoặc Guest
    if (displayName || accessToken) {
        if (guestNav) guestNav.classList.add('d-none');
        if (userProfileNav) userProfileNav.classList.remove('d-none');

        const nameEl = document.getElementById('userDisplayName');
        if (nameEl && displayName) {
            nameEl.innerText = displayName;
        }
    } else {
        // Nếu không có thông tin trong localStorage thì giữ theo server render hoặc guest
        const hasServerUser = userProfileNav && !userProfileNav.classList.contains('d-none');
        if (!hasServerUser) {
            if (guestNav) guestNav.classList.remove('d-none');
            if (userProfileNav) userProfileNav.classList.add('d-none');
        }
    }

    // 2. Lấy Roles của User
    let roles = [];
    const userRolesStr = localStorage.getItem('userRoles');
    if (userRolesStr) {
        try {
            roles = JSON.parse(userRolesStr);
        } catch (e) {
            console.error("Không đọc được userRoles:", e);
        }
    }

    // 3. Kiểm tra Admin
    const isAdmin =
        displayName === 'Admin' ||
        roles.includes('ROLE_ADMIN') ||
        roles.includes('ADMIN');

    if (isAdmin) {
        if (adminRoleNav) adminRoleNav.classList.remove('d-none');
    }

    // 4. Kiểm tra Merchant đã được duyệt
    const isMerchant =
        roles.includes('ROLE_MERCHANT') ||
        roles.includes('MERCHANT');

    if (isMerchant) {
        if (merchantRoleNav) merchantRoleNav.classList.remove('d-none');
        if (registerMerchantNav) registerMerchantNav.classList.add('d-none');
        if (merchantPendingNav) merchantPendingNav.classList.add('d-none');
    } else if ((displayName || accessToken) && !isAdmin) {
        checkMerchantStatus();
    }

    // Kiểm tra trạng thái đăng ký Merchant
    async function checkMerchantStatus() {
        try {
            const token = localStorage.getItem('accessToken');
            const headers = {};
            if (token) {
                headers['Authorization'] = 'Bearer ' + token;
            }

            const response = await fetch("/api/merchants/my-status", { headers: headers });
            if (!response.ok) {
                return;
            }

            const data = await response.json();

            // Mặc định ẩn hai trạng thái
            if (registerMerchantNav) registerMerchantNav.classList.add('d-none');
            if (merchantPendingNav) merchantPendingNav.classList.add('d-none');

            // Chưa đăng ký
            if (!data.registered) {
                if (registerMerchantNav) registerMerchantNav.classList.remove('d-none');
                return;
            }

            // Đang chờ Admin duyệt
            if (data.status === 'PENDING') {
                if (merchantPendingNav) merchantPendingNav.classList.remove('d-none');
                return;
            }

            // Bị từ chối
            if (data.status === 'REJECTED') {
                if (registerMerchantNav) registerMerchantNav.classList.remove('d-none');
                return;
            }

            // Đã được duyệt
            if (data.status === 'APPROVED') {
                if (merchantRoleNav) merchantRoleNav.classList.remove('d-none');
                return;
            }
        } catch (error) {
            console.warn("Lỗi kiểm tra trạng thái Merchant:", error);
        }
    }

    // 5. Xử lý chuyển hướng quay lại trang trước đó sau khi đăng nhập thành công
    const redirectUrl = sessionStorage.getItem('redirectAfterLogin');
    if (redirectUrl && (displayName || accessToken)) {
        sessionStorage.removeItem('redirectAfterLogin');
        window.location.href = redirectUrl;
    }
}

// Kích hoạt ngay khi load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initNavbar);
} else {
    initNavbar();
}

// 6. Xử lý Wishlist (Thích/Hủy thích món ăn) bằng AJAX trên toàn hệ thống
document.addEventListener('click', function(e) {
    const btnWishlist = e.target.closest('.btn-wishlist');
    if (btnWishlist) {
        e.preventDefault();
        e.stopPropagation();

        const isLoggedIn = localStorage.getItem('displayName') !== null || localStorage.getItem('accessToken') !== null;
        if (!isLoggedIn) {
            alert('Vui lòng đăng nhập để thực hiện chức năng này!');
            sessionStorage.setItem('redirectAfterLogin', window.location.href);
            window.location.href = '/login';
            return;
        }

        const foodId = btnWishlist.getAttribute('data-food-id');
        if (!foodId) return;

        const icon = btnWishlist.querySelector('i');
        const isLiked = icon.classList.contains('bi-heart-fill');

        const url = isLiked ? `/api/foods/${foodId}/unlike` : `/api/foods/${foodId}/like`;
        const token = localStorage.getItem('accessToken');
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        fetch(url, {
            method: 'POST',
            headers: headers
        })
        .then(response => {
            if (response.ok) {
                if (isLiked) {
                    icon.classList.remove('bi-heart-fill');
                    icon.classList.add('bi-heart');
                    btnWishlist.title = "Yêu thích";
                } else {
                    icon.classList.remove('bi-heart');
                    icon.classList.add('bi-heart-fill');
                    btnWishlist.title = "Bỏ yêu thích";
                }
            } else {
                alert('Có lỗi xảy ra, vui lòng thử lại sau!');
            }
        })
        .catch(err => {
            console.error("Lỗi Wishlist:", err);
            alert('Có lỗi kết nối mạng!');
        });
    }
});

// 7. Xử lý Follow/Unfollow Merchant bằng AJAX trên toàn hệ thống
document.addEventListener('click', function(e) {
    const btnFollow = e.target.closest('.btn-follow-merchant');
    if (btnFollow) {
        e.preventDefault();
        e.stopPropagation();

        const isLoggedIn = localStorage.getItem('displayName') !== null || localStorage.getItem('accessToken') !== null;
        if (!isLoggedIn) {
            alert('Vui lòng đăng nhập để thực hiện chức năng này!');
            sessionStorage.setItem('redirectAfterLogin', window.location.href);
            window.location.href = '/login';
            return;
        }

        const merchantId = btnFollow.getAttribute('data-merchant-id');
        if (!merchantId) return;

        const icon = btnFollow.querySelector('i');
        const span = btnFollow.querySelector('span');
        const isFollowed = btnFollow.classList.contains('btn-danger');

        const url = isFollowed ? `/api/merchants/${merchantId}/unfollow` : `/api/merchants/${merchantId}/follow`;
        const token = localStorage.getItem('accessToken');
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        fetch(url, {
            method: 'POST',
            headers: headers
        })
        .then(response => {
            if (response.ok) {
                if (isFollowed) {
                    btnFollow.classList.remove('btn-danger');
                    btnFollow.classList.add('btn-outline-danger');
                    if (icon) {
                        icon.classList.remove('bi-check-lg');
                        icon.classList.add('bi-plus-lg');
                    }
                    if (span) {
                        span.innerText = "Theo dõi";
                    }
                } else {
                    btnFollow.classList.remove('btn-outline-danger');
                    btnFollow.classList.add('btn-danger');
                    if (icon) {
                        icon.classList.remove('bi-plus-lg');
                        icon.classList.add('bi-check-lg');
                    }
                    if (span) {
                        span.innerText = "Đang theo dõi";
                    }
                }
            } else {
                alert('Có lỗi xảy ra, vui lòng thử lại sau!');
            }
        })
        .catch(err => {
            console.error("Lỗi Follow Merchant:", err);
            alert('Có lỗi kết nối mạng!');
        });
    }
});

// ==========================================
// LOGIC GIỎ HÀNG TOÀN CỤC (GLOBAL CART LOGIC)
// ==========================================

function getGlobalCart() {
    try {
        const cartStr = localStorage.getItem('mealchoice_cart');
        return cartStr ? JSON.parse(cartStr) : [];
    } catch (e) {
        console.error("Lỗi đọc giỏ hàng từ localStorage:", e);
        return [];
    }
}

function saveGlobalCart(cart) {
    try {
        localStorage.setItem('mealchoice_cart', JSON.stringify(cart));
        window.dispatchEvent(new Event('cartUpdated'));
    } catch (e) {
        console.error("Lỗi lưu giỏ hàng vào localStorage:", e);
    }
}

function addToCartGlobal(id, name, price, discountPrice, serviceFee = 0) {
    const cart = getGlobalCart();
    const existing = cart.find(item => item.id === id);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ id, name, price, discountPrice, serviceFee, quantity: 1 });
    }
    saveGlobalCart(cart);
    updateNavbarCartUI();
}

function changeQuantityGlobal(id, delta) {
    const cart = getGlobalCart();
    const index = cart.findIndex(item => item.id === id);
    if (index !== -1) {
        cart[index].quantity += delta;
        if (cart[index].quantity <= 0) {
            cart.splice(index, 1);
        }
    }
    saveGlobalCart(cart);
    updateNavbarCartUI();
}

function formatCurrencyGlobal(val) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(val)) + ' đ';
}

function updateNavbarCartUI() {
    const navbarCartCount = document.getElementById('navbarCartCount');
    const navbarCartItems = document.getElementById('navbarCartItems');
    const navbarCartTotal = document.getElementById('navbarCartTotal');
    const btnNavbarCheckout = document.getElementById('btnNavbarCheckout');

    if (!navbarCartCount) return;

    const cart = getGlobalCart();
    let totalItems = 0;
    let sumTotal = 0;

    if (navbarCartItems) {
        navbarCartItems.innerHTML = '';
    }

    if (cart.length === 0) {
        navbarCartCount.innerText = '0';
        if (navbarCartItems) {
            navbarCartItems.innerHTML = `
                <div class="text-center py-4 text-muted fs-8">
                    <i class="bi bi-basket fs-2 d-block opacity-50 mb-1"></i>
                    <span>Giỏ hàng trống</span>
                </div>
            `;
        }
        if (btnNavbarCheckout) {
            btnNavbarCheckout.disabled = true;
            btnNavbarCheckout.classList.add('opacity-50');
        }
    } else {
        if (btnNavbarCheckout) {
            btnNavbarCheckout.disabled = false;
            btnNavbarCheckout.classList.remove('opacity-50');
        }

        cart.forEach(item => {
            totalItems += item.quantity;
            const itemPrice = item.discountPrice != null ? item.discountPrice : item.price;
            sumTotal += itemPrice * item.quantity;

            if (navbarCartItems) {
                const div = document.createElement('div');
                div.className = 'd-flex align-items-center justify-content-between mb-2 pb-2 border-bottom border-light-subtle fs-8';
                div.innerHTML = `
                    <div class="pe-2 overflow-hidden flex-grow-1" style="max-width: 180px;">
                        <span class="d-block fw-semibold text-dark text-truncate" title="${item.name}">${item.name}</span>
                        <span class="text-danger fw-bold">${formatCurrencyGlobal(itemPrice)}</span>
                    </div>
                    <div class="d-flex align-items-center gap-1.5 flex-shrink-0">
                        <button class="btn btn-xs btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center border-1"
                                style="width: 18px; height: 18px; font-weight: bold; line-height: 1; padding: 0;"
                                onclick="changeQuantityGlobal(${item.id}, -1); event.stopPropagation();">
                            -
                        </button>
                        <span class="fw-bold text-dark" style="min-width: 12px; text-align: center;">${item.quantity}</span>
                        <button class="btn btn-xs btn-outline-danger rounded-circle d-flex align-items-center justify-content-center border-1"
                                style="width: 18px; height: 18px; font-weight: bold; line-height: 1; padding: 0;"
                                onclick="changeQuantityGlobal(${item.id}, 1); event.stopPropagation();">
                            +
                        </button>
                    </div>
                `;
                navbarCartItems.appendChild(div);
            }
        });

        navbarCartCount.innerText = totalItems;
    }

    if (navbarCartTotal) {
        navbarCartTotal.innerText = formatCurrencyGlobal(sumTotal);
    }
}

// Khởi tạo các sự kiện cho Navbar Cart
document.addEventListener('DOMContentLoaded', () => {
    updateNavbarCartUI();

    const btnNavbarCheckout = document.getElementById('btnNavbarCheckout');
    if (btnNavbarCheckout) {
        btnNavbarCheckout.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const cart = getGlobalCart();
            if (cart.length === 0) return;
            
            alert('Chức năng thanh toán đang được phát triển!');
        });
    }
});

