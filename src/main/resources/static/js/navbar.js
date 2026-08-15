
// ==================== AUTH & SESSION ====================

// Hàm xóa sạch Session và Cookie khi đăng xuất/hết hạn phiên
function clearAuthSession() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("displayName");
    localStorage.removeItem("userRoles");
    document.cookie = "accessToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
}

async function handleLogout(e) {
    e?.preventDefault();

    if (!confirm("Bạn có chắc chắn muốn đăng xuất không?")) return;

    try {
        const refreshToken = localStorage.getItem("refreshToken");

        await fetch("/api/auth/logout", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken: refreshToken || "" })
        });
    } catch (error) {
        console.error("Lỗi đăng xuất:", error);
    } finally {
        clearAuthSession();
        sessionStorage.clear();
        window.location.href = "/login";
    }
}


// ==================== NAVBAR & AUTH FETCH WRAPPER ====================

function getAuthHeaders() {
    const token = localStorage.getItem("accessToken");
    return token
        ? { Authorization: `Bearer ${token}` }
        : {};
}

// Biến Lock (Promise Singleton) tránh Race Condition khi nhiều request 401 cùng lúc
let refreshTokenPromise = null;

// Hàm thực hiện Refresh Token đơn nhiệm (Single-flight)
async function doRefreshToken() {
    if (refreshTokenPromise) {
        return refreshTokenPromise;
    }

    refreshTokenPromise = (async () => {
        const refreshToken = localStorage.getItem("refreshToken");
        if (!refreshToken) {
            clearAuthSession();
            window.location.href = "/login";
            return null;
        }

        try {
            const response = await fetch("/api/auth/refresh-token", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken })
            });

            if (response.ok) {
                const data = await response.json();
                if (data.accessToken) {
                    localStorage.setItem("accessToken", data.accessToken);
                    document.cookie = `accessToken=${data.accessToken}; path=/; max-age=900; SameSite=Lax`;
                    return data.accessToken;
                }
            }

            // Refresh Token hết hạn hoặc không hợp lệ -> Đăng xuất người dùng
            console.warn("Refresh Token đã hết hạn hoặc không hợp lệ. Đăng xuất...");
            clearAuthSession();
            window.location.href = "/login";
            return null;
        } catch (e) {
            console.error("Lỗi kết nối khi làm mới token:", e);
            return null;
        } finally {
            refreshTokenPromise = null;
        }
    })();

    return refreshTokenPromise;
}

// Wrapper fetch thông minh: Tự động refresh token và retry request khi dính HTTP 401
async function fetchWithAuth(url, options = {}) {
    options.headers = {
        ...options.headers,
        ...getAuthHeaders()
    };

    let response = await fetch(url, options);

    // Khi bị 401 -> Thử cấp lại token mới và retry request
    if (response.status === 401) {
        const newToken = await doRefreshToken();
        if (newToken) {
            options.headers = {
                ...options.headers,
                ...getAuthHeaders()
            };
            response = await fetch(url, options);
        }
    }
    return response;
}


function getUserRoles() {
    try {
        return JSON.parse(
            localStorage.getItem("userRoles") || "[]"
        );
    } catch (error) {
        console.error("Không đọc được userRoles:", error);
        return [];
    }
}


function initNavbar() {
    const displayName = localStorage.getItem("displayName");
    const accessToken = localStorage.getItem("accessToken");
    const roles = getUserRoles();

    const guestNav = document.getElementById("guestNav");
    const userProfileNav = document.getElementById("userProfileNav");
    const registerMerchantNav = document.getElementById("registerMerchantNav");
    const merchantPendingNav = document.getElementById("merchantPendingNav");
    const merchantRoleNav = document.getElementById("merchantRoleNav");
    const adminRoleNav = document.getElementById("adminRoleNav");

    const isLoggedIn = !!(displayName || accessToken);

    // User / Guest
    if (isLoggedIn) {
        guestNav?.classList.add("d-none");
        userProfileNav?.classList.remove("d-none");

        if (displayName) {
            document.getElementById("userDisplayName")?.replaceChildren(
                document.createTextNode(displayName)
            );
        }
    } else if (userProfileNav?.classList.contains("d-none")) {
        guestNav?.classList.remove("d-none");
    }

    // Admin
    const isAdmin =
        displayName === "Admin" ||
        roles.includes("ROLE_ADMIN") ||
        roles.includes("ADMIN");

    if (isAdmin) {
        adminRoleNav?.classList.remove("d-none");
    }

    // Merchant
    const isMerchant =
        roles.includes("ROLE_MERCHANT") ||
        roles.includes("MERCHANT");

    if (isMerchant) {
        merchantRoleNav?.classList.remove("d-none");
        registerMerchantNav?.classList.add("d-none");
        merchantPendingNav?.classList.add("d-none");
    } else if (isLoggedIn && !isAdmin) {
        checkMerchantStatus();
    }

    // Redirect sau login
    const redirectUrl =
        sessionStorage.getItem("redirectAfterLogin");

    if (redirectUrl && isLoggedIn) {
        sessionStorage.removeItem("redirectAfterLogin");
        window.location.href = redirectUrl;
    }
}


async function checkMerchantStatus() {
    try {
        const response = await fetchWithAuth("/api/merchant/my-status");

        if (!response.ok) return;

        const data = await response.json();

        const registerNav =
            document.getElementById("registerMerchantNav");

        const pendingNav =
            document.getElementById("merchantPendingNav");

        const merchantNav =
            document.getElementById("merchantRoleNav");

        registerNav?.classList.add("d-none");
        pendingNav?.classList.add("d-none");
        merchantNav?.classList.add("d-none");

        if (!data.registered || data.status === "REJECTED") {
            registerNav?.classList.remove("d-none");
        } else if (data.status === "PENDING") {
            pendingNav?.classList.remove("d-none");
        } else if (data.status === "APPROVED") {
            merchantNav?.classList.remove("d-none");
        }
    } catch (error) {
        console.warn("Lỗi kiểm tra trạng thái Merchant:", error);
    }
}


if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initNavbar);
} else {
    initNavbar();
}


// ==================== WISHLIST ====================

document.addEventListener("click", async e => {
    const button = e.target.closest(".btn-wishlist");
    if (!button) return;

    e.preventDefault();
    e.stopPropagation();

    const isLoggedIn =
        localStorage.getItem("displayName") ||
        localStorage.getItem("accessToken");

    if (!isLoggedIn) {
        alert("Vui lòng đăng nhập để thực hiện chức năng này!");
        sessionStorage.setItem(
            "redirectAfterLogin",
            window.location.href
        );
        window.location.href = "/login";
        return;
    }

    const foodId = button.dataset.foodId;
    const icon = button.querySelector("i");

    if (!foodId || !icon) return;

    const isLiked = icon.classList.contains("bi-heart-fill");
    const url = isLiked
        ? `/api/foods/${foodId}/unlike`
        : `/api/foods/${foodId}/like`;

    try {
        const response = await fetchWithAuth(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error("Wishlist request failed");

        icon.classList.toggle("bi-heart-fill", !isLiked);
        icon.classList.toggle("bi-heart", isLiked);
        button.title = isLiked ? "Yêu thích" : "Bỏ yêu thích";

    } catch (error) {
        console.error("Lỗi Wishlist:", error);
        alert("Có lỗi xảy ra, vui lòng thử lại sau!");
    }
});


// ==================== FOLLOW MERCHANT ====================

document.addEventListener("click", async e => {
    const button = e.target.closest(".btn-follow-merchant");
    if (!button) return;

    e.preventDefault();
    e.stopPropagation();

    const isLoggedIn =
        localStorage.getItem("displayName") ||
        localStorage.getItem("accessToken");

    if (!isLoggedIn) {
        alert("Vui lòng đăng nhập để thực hiện chức năng này!");
        sessionStorage.setItem(
            "redirectAfterLogin",
            window.location.href
        );
        window.location.href = "/login";
        return;
    }

    const merchantId = button.dataset.merchantId;
    const icon = button.querySelector("i");
    const text = button.querySelector("span");

    if (!merchantId) return;

    const isFollowed =
        button.classList.contains("btn-danger");

    const url = isFollowed
        ? `/api/merchants/${merchantId}/unfollow`
        : `/api/merchants/${merchantId}/follow`;

    try {
        const response = await fetchWithAuth(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error("Follow request failed");

        button.classList.toggle("btn-danger", !isFollowed);
        button.classList.toggle("btn-outline-danger", isFollowed);

        icon?.classList.toggle("bi-check-lg", !isFollowed);
        icon?.classList.toggle("bi-plus-lg", isFollowed);

        if (text) {
            text.innerText =
                isFollowed ? "Theo dõi" : "Đang theo dõi";
        }

    } catch (error) {
        console.error("Lỗi Follow Merchant:", error);
        alert("Có lỗi xảy ra, vui lòng thử lại sau!");
    }
});


// ==================== CART ====================

function getGlobalCart() {
    try {
        return JSON.parse(
            localStorage.getItem("mealchoice_cart") || "[]"
        );
    } catch (error) {
        console.error("Lỗi đọc giỏ hàng:", error);
        return [];
    }
}


function saveGlobalCart(cart) {
    try {
        localStorage.setItem(
            "mealchoice_cart",
            JSON.stringify(cart)
        );

        window.dispatchEvent(new Event("cartUpdated"));
    } catch (error) {
        console.error("Lỗi lưu giỏ hàng:", error);
    }
}


function addToCartGlobal(
    id,
    name,
    price,
    discountPrice,
    serviceFee = 0
) {
    const cart = getGlobalCart();
    const existing = cart.find(item => item.id === id);

    if (existing) {
        existing.quantity++;
    } else {
        cart.push({
            id,
            name,
            price,
            discountPrice,
            serviceFee,
            quantity: 1
        });
    }

    saveGlobalCart(cart);
    updateNavbarCartUI();
}


function changeQuantityGlobal(id, delta) {
    const cart = getGlobalCart();
    const item = cart.find(item => item.id === id);

    if (!item) return;

    item.quantity += delta;

    if (item.quantity <= 0) {
        cart.splice(cart.indexOf(item), 1);
    }

    saveGlobalCart(cart);
    updateNavbarCartUI();
}


function formatCurrencyGlobal(value) {
    return new Intl.NumberFormat("vi-VN")
        .format(Math.round(value)) + " đ";
}


function updateNavbarCartUI() {
    const countEl = document.getElementById("navbarCartCount");
    const itemsEl = document.getElementById("navbarCartItems");
    const totalEl = document.getElementById("navbarCartTotal");
    const checkoutBtn = document.getElementById("btnNavbarCheckout");

    if (!countEl) return;

    const cart = getGlobalCart();

    let totalItems = 0;
    let totalPrice = 0;

    if (itemsEl) {
        itemsEl.innerHTML = "";
    }

    if (!cart.length) {
        countEl.innerText = "0";

        if (itemsEl) {
            itemsEl.innerHTML = `
                <div class="text-center py-4 text-muted fs-8">
                    <i class="bi bi-basket fs-2 d-block opacity-50 mb-1"></i>
                    <span>Giỏ hàng trống</span>
                </div>
            `;
        }

        checkoutBtn?.setAttribute("disabled", "true");
        checkoutBtn?.classList.add("opacity-50");

    } else {
        checkoutBtn?.removeAttribute("disabled");
        checkoutBtn?.classList.remove("opacity-50");

        cart.forEach(item => {
            const price =
                item.discountPrice ?? item.price;

            totalItems += item.quantity;
            totalPrice += price * item.quantity;

            if (!itemsEl) return;

            const div = document.createElement("div");

            div.className =
                "d-flex align-items-center justify-content-between mb-2 pb-2 border-bottom border-light-subtle fs-8";

            div.innerHTML = `
                <div class="pe-2 overflow-hidden flex-grow-1"
                     style="max-width: 180px;">
                    <span class="d-block fw-semibold text-dark text-truncate"
                          title="${item.name}">
                        ${item.name}
                    </span>
                    <span class="text-danger fw-bold">
                        ${formatCurrencyGlobal(price)}
                    </span>
                </div>

                <div class="d-flex align-items-center gap-1.5 flex-shrink-0">
                    <button type="button"
                            class="btn btn-xs btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center"
                            style="width:18px;height:18px;padding:0;"
                            onclick="changeQuantityGlobal(${item.id}, -1); event.stopPropagation();">
                        -
                    </button>

                    <span class="fw-bold"
                          style="min-width:12px;text-align:center;">
                        ${item.quantity}
                    </span>

                    <button type="button"
                            class="btn btn-xs btn-outline-danger rounded-circle d-flex align-items-center justify-content-center"
                            style="width:18px;height:18px;padding:0;"
                            onclick="changeQuantityGlobal(${item.id}, 1); event.stopPropagation();">
                        +
                    </button>
                </div>
            `;

            itemsEl.appendChild(div);
        });

        countEl.innerText = totalItems;
    }

    if (totalEl) {
        totalEl.innerText =
            formatCurrencyGlobal(totalPrice);
    }
}


// ==================== CART EVENTS ====================

document.addEventListener("DOMContentLoaded", () => {
    updateNavbarCartUI();

    document
        .getElementById("btnNavbarCheckout")
        ?.addEventListener("click", e => {
            e.preventDefault();
            e.stopPropagation();

            if (getGlobalCart().length) {
                alert(
                    "Chức năng thanh toán đang được phát triển!"
                );
            }
        });
});


window.addEventListener("cartUpdated", updateNavbarCartUI);

window.addEventListener("storage", e => {
    if (e.key === "mealchoice_cart") {
        updateNavbarCartUI();
    }
});