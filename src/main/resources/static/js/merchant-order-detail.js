/**
 * Quản lý Chi tiết Đơn hàng của Merchant
 * File: static/js/merchant-order-detail.js
 */

let detailCancelOrderId = null;
let detailCancelModalInstance = null;
let detailRejectOrderId = null;
let detailRejectModalInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    const modalEl = document.getElementById('cancelOrderDetailModal');
    if (modalEl && typeof bootstrap !== 'undefined') {
        detailCancelModalInstance = new bootstrap.Modal(modalEl);
    }

    const rejectModalEl = document.getElementById('rejectDeliveryDetailModal');
    if (rejectModalEl && typeof bootstrap !== 'undefined') {
        detailRejectModalInstance = new bootstrap.Modal(rejectModalEl);
    }

    document.getElementById('btnConfirmCancelDetail')?.addEventListener('click', () => {
        if (!detailCancelOrderId) return;
        const reason = document.getElementById('cancelDetailReasonInput')?.value;
        executeCancelOrderDetail(detailCancelOrderId, reason);
    });

    document.getElementById('btnConfirmRejectDetail')?.addEventListener('click', () => {
        if (!detailRejectOrderId) return;
        const reason = document.getElementById('rejectDetailReasonInput')?.value;
        executeRejectDeliveryDetail(detailRejectOrderId, reason);
    });
});

// 1. NHẬN ĐƠN
async function handleAcceptOrderDetail(orderId) {
    if (!confirm('Bạn có chắc chắn muốn nhận đơn hàng này và bắt đầu chuẩn bị món?')) return;

    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/accept`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();
        if (result.success) {
            const badge = document.getElementById('detailStatusBadge');
            if (badge) {
                badge.className = 'badge badge-status-lg bg-primary text-white';
                badge.innerText = 'Đang chuẩn bị';
            }

            // Cập nhật header sang các nút hoàn thành / từ chối
            const actionHeader = document.getElementById('detailActionHeader');
            if (actionHeader) {
                actionHeader.innerHTML = `
                    <div class="d-flex gap-2" id="preparingButtonGroup">
                        <button type="button" class="btn btn-success rounded-pill px-4 py-2 fw-bold shadow-sm d-inline-flex align-items-center gap-2"
                                onclick="handleCompleteOrderDetail(${orderId})">
                            <i class="bi bi-check-circle-fill fs-5"></i>
                            <span>Đã nhận tiền & Hoàn thành</span>
                        </button>
                        <button type="button" class="btn btn-outline-danger rounded-pill px-4 py-2 fw-bold d-inline-flex align-items-center gap-2"
                                onclick="openRejectDeliveryModalDetail(${orderId}, '${result.data?.orderCode || ''}')">
                            <i class="bi bi-person-x fs-5"></i>
                            <span>Khách không nhận</span>
                        </button>
                    </div>
                    <a href="/merchant/orders" class="btn btn-light border rounded-pill px-4 py-2 text-secondary fw-semibold">
                        <i class="bi bi-list-ul me-1"></i>Về danh sách
                    </a>
                `;
            }

            alert('Đã nhận đơn hàng thành công! Đang chuyển sang chuẩn bị.');
        } else {
            alert(result.message || 'Không thể nhận đơn');
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối');
    }
}

// 2. MỞ MODAL HỦY ĐƠN (PENDING)
function openCancelModalDetail(orderId, orderCode) {
    detailCancelOrderId = orderId;
    const codeEl = document.getElementById('modalDetailOrderCode');
    if (codeEl) codeEl.innerText = orderCode;
    const reasonInput = document.getElementById('cancelDetailReasonInput');
    if (reasonInput) reasonInput.value = '';
    if (detailCancelModalInstance) {
        detailCancelModalInstance.show();
    }
}

// 3. THỰC HIỆN HỦY ĐƠN
async function executeCancelOrderDetail(orderId, reason) {
    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/cancel`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cancelReason: reason })
        });

        const result = await response.json();
        if (result.success) {
            if (detailCancelModalInstance) {
                detailCancelModalInstance.hide();
            }

            const badge = document.getElementById('detailStatusBadge');
            if (badge) {
                badge.className = 'badge badge-status-lg bg-danger text-white';
                badge.innerText = 'Đã hủy';
            }

            const group = document.getElementById('pendingButtonGroup');
            if (group) group.remove();

            alert('Đã hủy đơn hàng thành công.');
            location.reload();
        } else {
            alert(result.message || 'Không thể hủy đơn');
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối');
    }
}

// 4. HOÀN THÀNH ĐƠN (ĐÃ NHẬN TIỀN)
async function handleCompleteOrderDetail(orderId) {
    if (!confirm('Xác nhận bạn đã giao món và nhận tiền đầy đủ cho đơn hàng này?')) return;

    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/complete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();
        if (result.success) {
            const badge = document.getElementById('detailStatusBadge');
            if (badge) {
                badge.className = 'badge badge-status-lg bg-success text-white';
                badge.innerText = 'Hoàn thành';
            }

            const group = document.getElementById('preparingButtonGroup');
            if (group) group.remove();

            alert('Đơn hàng đã được ghi nhận hoàn thành thành công!');
            location.reload();
        } else {
            alert(result.message || 'Không thể hoàn thành đơn hàng');
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối');
    }
}

// 5. MỞ MODAL KHÁCH KHÔNG NHẬN HÀNG
function openRejectDeliveryModalDetail(orderId, orderCode) {
    detailRejectOrderId = orderId;
    const codeEl = document.getElementById('rejectDetailModalOrderCode');
    if (codeEl) codeEl.innerText = orderCode;
    const reasonInput = document.getElementById('rejectDetailReasonInput');
    if (reasonInput) reasonInput.value = 'Khách không nhận hàng';
    if (detailRejectModalInstance) {
        detailRejectModalInstance.show();
    }
}

// 6. THỰC HIỆN GHI NHẬN KHÁCH KHÔNG NHẬN HÀNG
async function executeRejectDeliveryDetail(orderId, reason) {
    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/failed-delivery`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cancelReason: reason })
        });

        const result = await response.json();
        if (result.success) {
            if (detailRejectModalInstance) {
                detailRejectModalInstance.hide();
            }

            const badge = document.getElementById('detailStatusBadge');
            if (badge) {
                badge.className = 'badge badge-status-lg bg-danger text-white';
                badge.innerText = 'Đã hủy';
            }

            const group = document.getElementById('preparingButtonGroup');
            if (group) group.remove();

            alert('Đã ghi nhận đơn hàng bị hủy do khách không nhận hàng.');
            location.reload();
        } else {
            alert(result.message || 'Không thể ghi nhận hủy đơn');
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối');
    }
}
