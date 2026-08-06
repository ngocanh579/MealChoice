(() => {
    const page = document.body.dataset.page;
    const alertBox = document.getElementById('pageAlert');
    const escapeHtml = MealChoice.escapeHtml;

    function statusBadge(status) {
        const safeStatus = escapeHtml(status || 'PENDING');
        return `<span class="mc-badge status-${safeStatus}">${safeStatus}</span>`;
    }

    async function loadList() {
        if (!MealChoice.requireLogin()) return;
        const keyword = document.getElementById('keyword')?.value.trim() || '';
        const status = document.getElementById('statusFilter')?.value || '';
        const query = new URLSearchParams();
        if (keyword) query.set('keyword', keyword);
        if (status) query.set('status', status);

        const tbody = document.getElementById('merchantRows');
        tbody.innerHTML = '<tr><td colspan="6" class="mc-empty">Đang tải danh sách...</td></tr>';
        try {
            const result = await MealChoice.request(`/api/admin/merchants?${query}`);
            renderSummary(result.data);
            renderRows(result.data);
        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="6" class="mc-empty">${escapeHtml(error.message)}</td></tr>`;
            MealChoice.alert(alertBox, 'error', error.message);
        }
    }

    function renderSummary(merchants) {
        const counts = { ALL: merchants.length, PENDING: 0, APPROVED: 0, BLOCKED: 0 };
        merchants.forEach(merchant => {
            if (Object.hasOwn(counts, merchant.status)) counts[merchant.status] += 1;
        });
        Object.entries(counts).forEach(([key, value]) => {
            const element = document.getElementById(`count${key}`);
            if (element) element.textContent = String(value);
        });
    }

    function renderRows(merchants) {
        const tbody = document.getElementById('merchantRows');
        if (!merchants.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="mc-empty">Không có merchant phù hợp.</td></tr>';
            return;
        }

        tbody.innerHTML = merchants.map(merchant => `
            <tr>
                <td>
                    <strong>${escapeHtml(merchant.restaurantName)}</strong><br>
                    <small>${escapeHtml(merchant.ownerName || 'Chưa cập nhật')}</small>
                </td>
                <td>${escapeHtml(merchant.email)}<br><small>${escapeHtml(merchant.phone)}</small></td>
                <td>${statusBadge(merchant.status)}</td>
                <td>${merchant.loyalPartner ? '<span class="mc-badge status-APPROVED">Thân thiết</span>' : '—'}</td>
                <td>${merchant.accountActive ? 'Hoạt động' : 'Đã khóa'}</td>
                <td>
                    <div class="mc-actions" style="margin:0">
                        <a class="mc-btn mc-btn-outline mc-btn-sm" href="/admin/merchants/${merchant.id}">Chi tiết</a>
                        ${merchant.status === 'PENDING' ? `
                            <button class="mc-btn mc-btn-success mc-btn-sm" data-action="approve" data-id="${merchant.id}">Duyệt</button>
                            <button class="mc-btn mc-btn-danger mc-btn-sm" data-action="reject" data-id="${merchant.id}">Từ chối</button>
                        ` : ''}
                        <button class="mc-btn mc-btn-warning mc-btn-sm" data-action="lock" data-id="${merchant.id}" data-locked="${merchant.status !== 'BLOCKED'}">
                            ${merchant.status === 'BLOCKED' ? 'Mở khóa' : 'Khóa'}
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    async function performAction(action, id, element) {
        let url;
        let payload;
        if (action === 'approve') {
            url = `/api/admin/merchants/${id}/decision`;
            payload = { decision: 'APPROVE', reason: '' };
        } else if (action === 'reject') {
            const reason = prompt('Nhập lý do từ chối merchant:');
            if (!reason?.trim()) return;
            url = `/api/admin/merchants/${id}/decision`;
            payload = { decision: 'REJECT', reason: reason.trim() };
        } else if (action === 'lock') {
            const locked = element.dataset.locked === 'true';
            if (!confirm(locked ? 'Khóa merchant này?' : 'Mở khóa merchant này?')) return;
            url = `/api/admin/merchants/${id}/lock`;
            payload = { locked };
        } else if (action === 'loyal') {
            const approved = element.dataset.approved === 'true';
            url = `/api/admin/merchants/${id}/loyal-partner`;
            payload = { approved };
        } else {
            return;
        }

        try {
            const result = await MealChoice.request(url, {
                method: 'PATCH',
                body: JSON.stringify(payload)
            });
            MealChoice.alert(alertBox, 'success', result.message);
            if (page === 'admin-merchant-list') await loadList();
            if (page === 'admin-merchant-detail') renderDetail(result.data);
        } catch (error) {
            MealChoice.alert(alertBox, 'error', error.message);
        }
    }

    async function loadDetail() {
        if (!MealChoice.requireLogin()) return;
        const root = document.getElementById('merchantDetail');
        const id = root.dataset.merchantId;
        try {
            const result = await MealChoice.request(`/api/admin/merchants/${id}`);
            renderDetail(result.data);
        } catch (error) {
            MealChoice.alert(alertBox, 'error', error.message);
        }
    }

    function renderDetail(merchant) {
        const root = document.getElementById('merchantDetail');
        root.dataset.merchantId = merchant.id;
        document.getElementById('detailRestaurantName').textContent = merchant.restaurantName;
        document.getElementById('detailStatus').innerHTML = statusBadge(merchant.status);
        const values = {
            detailOwnerName: merchant.ownerName || 'Chưa cập nhật',
            detailEmail: merchant.email,
            detailPhone: merchant.phone,
            detailAddress: merchant.address,
            detailHours: merchant.openTime && merchant.closeTime ? `${merchant.openTime} – ${merchant.closeTime}` : 'Chưa cập nhật',
            detailAccount: merchant.accountActive ? 'Đang hoạt động' : 'Đã khóa',
            detailLoyal: merchant.loyalPartner ? 'Đối tác thân thiết' : 'Merchant thường',
            detailReview: merchant.reviewNote || 'Chưa có ghi chú',
            detailReviewer: merchant.reviewedBy || 'Chưa xét duyệt'
        };
        Object.entries(values).forEach(([id, value]) => {
            document.getElementById(id).textContent = value;
        });

        const actions = document.getElementById('detailActions');
        actions.innerHTML = `
            ${merchant.status === 'PENDING' ? `
                <button class="mc-btn mc-btn-success" data-action="approve" data-id="${merchant.id}">Duyệt merchant</button>
                <button class="mc-btn mc-btn-danger" data-action="reject" data-id="${merchant.id}">Từ chối</button>
            ` : ''}
            <button class="mc-btn mc-btn-warning" data-action="lock" data-id="${merchant.id}" data-locked="${merchant.status !== 'BLOCKED'}">
                ${merchant.status === 'BLOCKED' ? 'Mở khóa merchant' : 'Khóa merchant'}
            </button>
            ${merchant.status === 'APPROVED' ? `
                <button class="mc-btn mc-btn-outline" data-action="loyal" data-id="${merchant.id}" data-approved="${!merchant.loyalPartner}">
                    ${merchant.loyalPartner ? 'Hủy đối tác thân thiết' : 'Duyệt đối tác thân thiết'}
                </button>
            ` : ''}
        `;
    }

    document.addEventListener('click', event => {
        const button = event.target.closest('[data-action][data-id]');
        if (button) performAction(button.dataset.action, button.dataset.id, button);
    });

    if (page === 'admin-merchant-list') {
        document.getElementById('filterForm')?.addEventListener('submit', event => {
            event.preventDefault();
            loadList();
        });
        document.getElementById('resetFilter')?.addEventListener('click', () => {
            document.getElementById('filterForm').reset();
            loadList();
        });
        loadList();
    }
    if (page === 'admin-merchant-detail') loadDetail();
})();
