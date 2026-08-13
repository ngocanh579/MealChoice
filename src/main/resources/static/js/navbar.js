// Xử lý đăng xuất (Scope toàn cục để html onclick gọi trực tiếp)
async function handleLogout(e) {
    e.preventDefault();

    if (confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST'
            });
        } catch (error) {
            console.error("Lỗi đăng xuất:", error);
        } finally {
            document.cookie =
                "accessToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";

            localStorage.clear();
            window.location.href = "/login";
        }
    }
}

// Chạy các logic hiển thị Navbar sau khi DOM đã được load xong
document.addEventListener('DOMContentLoaded', function() {
    const displayName = localStorage.getItem('displayName');

    const guestNav = document.getElementById('guestNav');
    const userProfileNav = document.getElementById('userProfileNav');
    const registerMerchantNav = document.getElementById('registerMerchantNav');
    const merchantPendingNav = document.getElementById('merchantPendingNav');
    const merchantRoleNav = document.getElementById('merchantRoleNav');
    const adminRoleNav = document.getElementById('adminRoleNav');

    // Hiển thị User hoặc Guest
    if (displayName) {
        guestNav?.classList.add('d-none');
        userProfileNav?.classList.remove('d-none');

        const nameEl = document.getElementById('userDisplayName');

        if (nameEl) {
            nameEl.innerText = displayName;
        }
    } else {
        guestNav?.classList.remove('d-none');
        userProfileNav?.classList.add('d-none');
    }

    // Lấy Role của User
    let roles = [];
    const userRolesStr = localStorage.getItem('userRoles');

    if (userRolesStr) {
        try {
            roles = JSON.parse(userRolesStr);
        } catch (e) {
            console.error("Không đọc được userRoles:", e);
        }
    }

    // Kiểm tra Admin
    const isAdmin =
        displayName === 'Admin' ||
        roles.includes('ROLE_ADMIN') ||
        roles.includes('ADMIN');

    if (isAdmin) {
        adminRoleNav?.classList.remove('d-none');
    }

    // Kiểm tra Merchant đã được duyệt
    const isMerchant =
        roles.includes('ROLE_MERCHANT') ||
        roles.includes('MERCHANT');

    if (isMerchant) {
        merchantRoleNav?.classList.remove('d-none');
        registerMerchantNav?.classList.add('d-none');
        merchantPendingNav?.classList.add('d-none');
    } else if (displayName && !isAdmin) {
        checkMerchantStatus();
    }

    // Kiểm tra trạng thái đăng ký Merchant
    async function checkMerchantStatus() {
        try {
            const response = await fetch("/api/merchants/my-status");

            if (!response.ok) {
                throw new Error("Không thể lấy trạng thái Merchant");
            }

            const data = await response.json();

            // Mặc định ẩn hai trạng thái
            registerMerchantNav?.classList.add('d-none');
            merchantPendingNav?.classList.add('d-none');

            // Chưa đăng ký
            if (!data.registered) {
                registerMerchantNav?.classList.remove('d-none');
                return;
            }

            // Đang chờ Admin duyệt
            if (data.status === 'PENDING') {
                merchantPendingNav?.classList.remove('d-none');
                return;
            }

            // Bị từ chối
            if (data.status === 'REJECTED') {
                registerMerchantNav?.classList.remove('d-none');
                return;
            }

            // Đã được duyệt nhưng Role chưa cập nhật
            if (data.status === 'APPROVED') {
                if (registerMerchantNav) {
                    registerMerchantNav.classList.add('d-none');
                }
                if (merchantPendingNav) {
                    merchantPendingNav.classList.add('d-none');
                }
                if (merchantRoleNav) {
                    merchantRoleNav.classList.remove('d-none');
                }
                return;
            }

        } catch (error) {
            console.error("Lỗi kiểm tra trạng thái Merchant:", error);
        }
    }

    // 1. Xử lý chuyển hướng quay lại trang trước đó sau khi đăng nhập thành công
    const redirectUrl = sessionStorage.getItem('redirectAfterLogin');
    if (redirectUrl && displayName) {
        sessionStorage.removeItem('redirectAfterLogin');
        window.location.href = redirectUrl;
    }

    // 2. Xử lý Wishlist (Thích/Hủy thích món ăn) bằng AJAX trên toàn hệ thống
    document.addEventListener('click', function(e) {
        const btnWishlist = e.target.closest('.btn-wishlist');
        if (btnWishlist) {
            e.preventDefault();
            e.stopPropagation();
            
            // Kiểm tra đăng nhập
            const isLoggedIn = localStorage.getItem('displayName') !== null;
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
            
            // Gọi API backend để cập nhật trạng thái thích
            const url = isLiked ? `/api/foods/${foodId}/unlike` : `/api/foods/${foodId}/like`;
            
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
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

    // 3. Xử lý Follow/Unfollow Merchant bằng AJAX trên toàn hệ thống
    document.addEventListener('click', function(e) {
        const btnFollow = e.target.closest('.btn-follow-merchant');
        if (btnFollow) {
            e.preventDefault();
            e.stopPropagation();
            
            // Kiểm tra đăng nhập
            const isLoggedIn = localStorage.getItem('displayName') !== null;
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
            const isFollowed = btnFollow.classList.contains('btn-danger'); // Nếu có class btn-danger tức là đã theo dõi
            
            // Gọi API backend để cập nhật trạng thái follow
            const url = isFollowed ? `/api/merchants/${merchantId}/unfollow` : `/api/merchants/${merchantId}/follow`;
            
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                if (response.ok) {
                    if (isFollowed) {
                        // Chuyển về trạng thái chưa theo dõi (btn-outline-danger)
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
                        // Chuyển về trạng thái đang theo dõi (btn-danger)
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
});
