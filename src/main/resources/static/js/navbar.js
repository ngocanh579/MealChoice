(function () {
    if (window.__MEALCHOICE_NAVBAR_INITIALIZED__) return;
    window.__MEALCHOICE_NAVBAR_INITIALIZED__ = true;

    function clearAuthSession() {
        ["accessToken", "refreshToken", "displayName", "userRoles"]
            .forEach(key => localStorage.removeItem(key));

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

    function getAuthHeaders() {
        const token = localStorage.getItem("accessToken");
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    let refreshTokenPromise = null;

    async function doRefreshToken() {
        if (refreshTokenPromise) return refreshTokenPromise;

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
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify({ refreshToken })
                });

                if (response.ok) {
                    const data = await response.json();

                    if (data.accessToken) {
                        localStorage.setItem("accessToken", data.accessToken);

                        document.cookie =
                            `accessToken=${data.accessToken}; path=/; max-age=86400; SameSite=Lax`;

                        if (data.refreshToken) {
                            localStorage.setItem("refreshToken", data.refreshToken);
                        }

                        return data.accessToken;
                    }
                }

                clearAuthSession();
                window.location.href = "/login";
                return null;
            } catch (error) {
                console.error("Lỗi kết nối khi làm mới token:", error);
                return null;
            } finally {
                refreshTokenPromise = null;
            }
        })();

        return refreshTokenPromise;
    }

    async function fetchWithAuth(url, options = {}) {
        options.headers = {
            Accept: "application/json",
            ...options.headers,
            ...getAuthHeaders()
        };

        let response = await fetch(url, options);

        const isUnauthorized =
            response.status === 401 ||
            (response.redirected && response.url.includes("/login"));

        if (isUnauthorized) {
            console.log("Token hết hạn, đang refresh...");

            const newToken = await doRefreshToken();

            if (newToken) {
                options.headers = {
                    Accept: "application/json",
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
            return JSON.parse(localStorage.getItem("userRoles") || "[]");
        } catch (error) {
            console.error("Không đọc được userRoles:", error);
            return [];
        }
    }

    function hideMerchantNavigation() {
        document.getElementById("registerMerchantNav")?.classList.add("d-none");
        document.getElementById("merchantPendingNav")?.classList.add("d-none");
        document.getElementById("merchantRoleNav")?.classList.add("d-none");
    }

    function showMerchantRegister() {
        document.getElementById("registerMerchantNav")?.classList.remove("d-none");
        document.getElementById("merchantPendingNav")?.classList.add("d-none");
        document.getElementById("merchantRoleNav")?.classList.add("d-none");
    }

    function showMerchantPending() {
        document.getElementById("registerMerchantNav")?.classList.add("d-none");
        document.getElementById("merchantPendingNav")?.classList.remove("d-none");
        document.getElementById("merchantRoleNav")?.classList.add("d-none");
    }

    function showMerchantChannel() {
        document.getElementById("registerMerchantNav")?.classList.add("d-none");
        document.getElementById("merchantPendingNav")?.classList.add("d-none");
        document.getElementById("merchantRoleNav")?.classList.remove("d-none");
    }

    function initNavbar() {
        const displayName = localStorage.getItem("displayName");
        const accessToken = localStorage.getItem("accessToken");
        const roles = getUserRoles();

        const guestAuthNav = document.getElementById("guestAuthNav");
        const userDropdownNav = document.getElementById("userDropdownNav");
        const adminRoleNav = document.getElementById("adminRoleNav");
        const deliveryPartnerAdminNav = document.getElementById("deliveryPartnerAdminNav");

        const isLoggedIn = !!(displayName || accessToken);

        const isAdmin =
            displayName === "Admin" ||
            roles.includes("ROLE_ADMIN") ||
            roles.includes("ADMIN");

        if (!isLoggedIn) {
            guestAuthNav?.classList.remove("d-none");
            userDropdownNav?.classList.add("d-none");
            adminRoleNav?.classList.add("d-none");
            deliveryPartnerAdminNav?.classList.add("d-none");
            hideMerchantNavigation();
            return;
        }

        guestAuthNav?.classList.add("d-none");
        userDropdownNav?.classList.remove("d-none");

        const userName = document.getElementById("navbarUserName");

        if (userName && displayName) {
            userName.replaceChildren(document.createTextNode(displayName));
        }

        if (isAdmin) {
            adminRoleNav?.classList.remove("d-none");
            deliveryPartnerAdminNav?.classList.remove("d-none");
            hideMerchantNavigation();
        } else {
            adminRoleNav?.classList.add("d-none");
            deliveryPartnerAdminNav?.classList.add("d-none");
            checkMerchantStatus();
        }

        const redirectUrl = sessionStorage.getItem("redirectAfterLogin");

        if (redirectUrl) {
            sessionStorage.removeItem("redirectAfterLogin");
            window.location.href = redirectUrl;
        }
    }

    async function checkMerchantStatus() {
        const token = localStorage.getItem("accessToken");

        if (!token) {
            console.log("[Merchant] Không có accessToken");
            return;
        }

        try {
            console.log("[Merchant] Đang kiểm tra /api/merchant/my-status...");

            const response = await fetchWithAuth("/api/merchant/my-status");

            console.log("[Merchant] Response status:", response.status);
            console.log("[Merchant] Response URL:", response.url);

            if (!response.ok) {
                console.error("[Merchant] API lỗi:", response.status);
                return;
            }

            const contentType = response.headers.get("content-type");
            console.log("[Merchant] Content-Type:", contentType);

            if (!contentType?.includes("application/json")) {
                console.error("[Merchant] Response không phải JSON");
                return;
            }

            const data = await response.json();

            console.log("[Merchant] API my-status trả về:", data);
            console.log("[Merchant] registered:", data.registered);
            console.log("[Merchant] status:", data.status);

            hideMerchantNavigation();

            if (!data.registered) {
                console.log("[Merchant] Chưa đăng ký → hiện Đăng ký Merchant");
                showMerchantRegister();
                return;
            }

            if (data.status === "PENDING") {
                console.log("[Merchant] PENDING → hiện Chờ duyệt");
                showMerchantPending();
                return;
            }

            if (data.status === "REJECTED") {
                console.log("[Merchant] REJECTED → hiện Đăng ký Merchant");
                showMerchantRegister();
                return;
            }

            if (data.status === "APPROVED") {
                console.log("[Merchant] APPROVED → hiện Kênh Merchant");
                showMerchantChannel();
                return;
            }

            console.warn("[Merchant] Status không xác định:", data.status);
        } catch (error) {
            console.error("[Merchant] Lỗi kiểm tra trạng thái:", error);
        }
    }

    function initMerchantBlockedModal() {
        const button = document.getElementById("merchantChannelBtn");

        if (!button) return;

        button.addEventListener("click", function (e) {
            if (this.dataset.blocked !== "true") return;

            e.preventDefault();

            const modalElement = document.getElementById("merchantBlockedModal");

            if (modalElement && typeof bootstrap !== "undefined") {
                bootstrap.Modal
                    .getOrCreateInstance(modalElement)
                    .show();
            }
        });
    }

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
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("Wishlist request failed");
            }

            icon.classList.toggle("bi-heart-fill", !isLiked);
            icon.classList.toggle("bi-heart", isLiked);
            button.title = isLiked ? "Yêu thích" : "Bỏ yêu thích";
        } catch (error) {
            console.error("Lỗi Wishlist:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại sau!");
        }
    });

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
        if (!merchantId) return;

        const isFollowed = button.classList.contains("btn-danger");

        const url = isFollowed
            ? `/api/merchants/${merchantId}/unfollow`
            : `/api/merchants/${merchantId}/follow`;

        try {
            const response = await fetchWithAuth(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("Follow request failed: " + response.status);
            }

            let data = {};
            const contentType = response.headers.get("content-type");

            if (contentType?.includes("application/json")) {
                data = await response.json();
            }

            const newFollowed =
                typeof data.followed !== "undefined"
                    ? data.followed
                    : !isFollowed;

            document
                .querySelectorAll(
                    `.btn-follow-merchant[data-merchant-id="${merchantId}"]`
                )
                .forEach(btn => {
                    btn.classList.toggle("btn-danger", newFollowed);
                    btn.classList.toggle("btn-outline-danger", !newFollowed);

                    const icon = btn.querySelector("i");
                    const text = btn.querySelector("span");

                    if (icon) {
                        icon.className = newFollowed
                            ? "bi bi-check-lg me-1"
                            : "bi bi-plus-lg me-1";
                    }

                    if (text) {
                        text.innerText = newFollowed
                            ? "Đang theo dõi"
                            : "Theo dõi";
                    }
                });

            const followerCountEl =
                document.getElementById("storeFollowerCount");

            if (
                followerCountEl &&
                typeof data.followerCount !== "undefined"
            ) {
                followerCountEl.innerText = data.followerCount;
            }
        } catch (error) {
            console.error("Lỗi Follow Merchant:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại sau!");
        }
    });

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
        idOrItem,
        name,
        price,
        discountPrice,
        serviceFee = 0,
        merchantId = null,
        merchantName = null,
        image = null
    ) {
        const itemToAdd =
            typeof idOrItem === "object" && idOrItem !== null
                ? {
                    id: idOrItem.id,
                    name: idOrItem.name || idOrItem.foodName || "Món ăn",
                    price: Number(idOrItem.price || 0),
                    discountPrice:
                        idOrItem.discountPrice != null
                            ? Number(idOrItem.discountPrice)
                            : Number(idOrItem.price || 0),
                    serviceFee: Number(idOrItem.serviceFee || 0),
                    merchantId: idOrItem.merchantId || null,
                    merchantName: idOrItem.merchantName || null,
                    image: idOrItem.image || null,
                    quantity: Number(idOrItem.quantity || 1)
                }
                : {
                    id: idOrItem,
                    name: name || "Món ăn",
                    price: Number(price || 0),
                    discountPrice:
                        discountPrice != null
                            ? Number(discountPrice)
                            : Number(price || 0),
                    serviceFee: Number(serviceFee || 0),
                    merchantId: merchantId || null,
                    merchantName: merchantName || null,
                    image: image || null,
                    quantity: 1
                };

        const cart = getGlobalCart();

        if (cart.length && itemToAdd.merchantId) {
            const existingMerchantId = cart[0].merchantId;

            if (
                existingMerchantId &&
                existingMerchantId !== itemToAdd.merchantId
            ) {
                const confirmClear = confirm(
                    "Giỏ hàng của bạn đang có món của cửa hàng khác. Bạn có muốn xóa giỏ hàng cũ để thêm món của quán này không?"
                );

                if (confirmClear) {
                    cart.length = 0;
                } else {
                    return;
                }
            }
        }

        const existing = cart.find(item => item.id === itemToAdd.id);

        if (existing) {
            existing.quantity += itemToAdd.quantity;

            if (itemToAdd.merchantId && !existing.merchantId) {
                existing.merchantId = itemToAdd.merchantId;
            }

            if (itemToAdd.merchantName && !existing.merchantName) {
                existing.merchantName = itemToAdd.merchantName;
            }

            if (itemToAdd.image && !existing.image) {
                existing.image = itemToAdd.image;
            }
        } else {
            cart.push(itemToAdd);
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

        if (itemsEl) itemsEl.innerHTML = "";

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
                const price = item.discountPrice ?? item.price;

                totalItems += item.quantity;
                totalPrice += price * item.quantity;

                if (!itemsEl) return;

                const div = document.createElement("div");

                div.className =
                    "d-flex align-items-center justify-content-between mb-2 pb-2 border-bottom border-light-subtle fs-8";

                div.innerHTML = `
                    <div class="pe-2 overflow-hidden flex-grow-1" style="max-width:180px;">
                        <span class="d-block fw-semibold text-dark text-truncate" title="${item.name}">
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
                        <span class="fw-bold" style="min-width:12px;text-align:center;">
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
            totalEl.innerText = formatCurrencyGlobal(totalPrice);
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        initNavbar();
        initMerchantBlockedModal();
        updateNavbarCartUI();

        document
            .getElementById("btnNavbarCheckout")
            ?.addEventListener("click", e => {
                e.preventDefault();
                e.stopPropagation();

                if (getGlobalCart().length) {
                    window.location.href = "/checkout";
                }
            });
    });

    window.addEventListener("cartUpdated", updateNavbarCartUI);

    window.addEventListener("storage", e => {
        if (e.key === "mealchoice_cart") {
            updateNavbarCartUI();
        }
    });

    window.clearAuthSession = clearAuthSession;
    window.handleLogout = handleLogout;
    window.fetchWithAuth = fetchWithAuth;
    window.addToCartGlobal = addToCartGlobal;
    window.addToGlobalCart = addToCartGlobal;
    window.getGlobalCart = getGlobalCart;
    window.saveGlobalCart = saveGlobalCart;
    window.updateNavbarCartUI = updateNavbarCartUI;
    window.changeQuantityGlobal = changeQuantityGlobal;
    window.formatCurrencyGlobal = formatCurrencyGlobal;
})();