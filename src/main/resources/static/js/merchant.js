(() => {
    const page = document.body.dataset.page;
    const alertBox = document.getElementById('pageAlert');

    function setBusy(form, busy) {
        form?.classList.toggle('mc-loading', busy);
        form?.querySelectorAll('button, input, select, textarea').forEach(element => {
            element.disabled = busy || element.dataset.alwaysDisabled === 'true';
        });
    }

    async function registerMerchant(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const data = Object.fromEntries(new FormData(form));

        if (data.password !== data.confirmPassword) {
            MealChoice.alert(alertBox, 'error', 'Mật khẩu nhập lại không khớp');
            return;
        }

        setBusy(form, true);
        try {
            const response = await fetch('/api/merchants/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const body = await response.text();
            if (!response.ok) {
                try {
                    const error = JSON.parse(body);
                    throw new Error(error.message || 'Đăng ký merchant thất bại');
                } catch (parseError) {
                    throw parseError instanceof SyntaxError ? new Error(body || 'Đăng ký merchant thất bại') : parseError;
                }
            }
            MealChoice.alert(alertBox, 'success', 'Đăng ký merchant thành công. Hồ sơ đang chờ Admin xét duyệt.');
            form.reset();
        } catch (error) {
            MealChoice.alert(alertBox, 'error', error.message);
        } finally {
            setBusy(form, false);
        }
    }

    function fillProfile(merchant) {
        for (const field of ['ownerName', 'restaurantName', 'email', 'phone', 'address', 'openTime', 'closeTime']) {
            const element = document.getElementById(field);
            if (element) element.value = merchant[field] || '';
        }
        const status = document.getElementById('merchantStatus');
        if (status) {
            status.className = `mc-badge status-${merchant.status || 'PENDING'}`;
            status.textContent = merchant.status || 'PENDING';
        }
        const loyal = document.getElementById('loyalPartner');
        if (loyal) loyal.hidden = !merchant.loyalPartner;
    }

    async function loadProfile() {
        if (!MealChoice.requireLogin()) return;
        const form = document.getElementById('merchantProfileForm');
        setBusy(form, true);
        try {
            const result = await MealChoice.request('/api/merchant/profile');
            fillProfile(result.data);
        } catch (error) {
            MealChoice.alert(alertBox, 'error', error.message);
            if (error.status === 401 || error.status === 403) {
                setTimeout(() => window.location.href = '/login', 1200);
            }
        } finally {
            setBusy(form, false);
        }
    }

    async function updateProfile(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const data = Object.fromEntries(new FormData(form));
        setBusy(form, true);
        try {
            const result = await MealChoice.request('/api/merchant/profile', {
                method: 'PUT',
                body: JSON.stringify({
                    restaurantName: data.restaurantName,
                    address: data.address,
                    openTime: data.openTime,
                    closeTime: data.closeTime
                })
            });
            fillProfile(result.data);
            MealChoice.alert(alertBox, 'success', result.message);
        } catch (error) {
            MealChoice.alert(alertBox, 'error', error.message);
        } finally {
            setBusy(form, false);
        }
    }

    if (page === 'merchant-register') {
        document.getElementById('merchantRegisterForm')?.addEventListener('submit', registerMerchant);
    }
    if (page === 'merchant-profile') {
        document.getElementById('merchantProfileForm')?.addEventListener('submit', updateProfile);
        loadProfile();
    }
})();
