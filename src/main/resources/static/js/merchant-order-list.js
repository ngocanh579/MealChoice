/**
 * Quản lý Danh sách Đơn hàng của Merchant
 * File: static/js/merchant-order-list.js
 */

let currentCancelOrderId = null;
let cancelModalInstance = null;
let currentRejectOrderId = null;
let rejectModalInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    const cancelModalEl = document.getElementById('cancelOrderModal');
    if (cancelModalEl && typeof bootstrap !== 'undefined') {
        cancelModalInstance = new bootstrap.Modal(cancelModalEl);
    }

    const rejectModalEl = document.getElementById('rejectDeliveryModal');
    if (rejectModalEl && typeof bootstrap !== 'undefined') {
        rejectModalInstance = new bootstrap.Modal(rejectModalEl);
    }

    document.getElementById('btnConfirmCancelOrder')?.addEventListener('click', () => {
        if (!currentCancelOrderId) return;
        const reason = document.getElementById('cancelReasonInput')?.value;
        executeCancelOrder(currentCancelOrderId, reason);
    });

    document.getElementById('btnConfirmRejectDelivery')?.addEventListener('click', () => {
        if (!currentRejectOrderId) return;
        const reason = document.getElementById('rejectReasonInput')?.value;
        executeRejectDelivery(currentRejectOrderId, reason);
    });
});

// 1. XỬ LÝ NHẬN ĐƠN (ACCEPT ORDER)
async function handleAcceptOrder(orderId) {
    if (!confirm('Bạn có chắc chắn muốn nhận đơn hàng này và bắt đầu chuẩn bị món?')) return;

    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/accept`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.success) {
            // Cập nhật trạng thái hiển thị
            const statusCol = document.getElementById(`order-status-col-${orderId}`);
            if (statusCol) {
                statusCol.innerHTML = `
                    <span class="badge badge-status bg-primary text-white">
                        Đang chuẩn bị
                    </span>
                `;
            }

            // Cập nhật các nút hành động sang nhóm PREPARING
            const actionCol = document.getElementById(`order-action-col-${orderId}`);
            if (actionCol) {
                actionCol.innerHTML = `
                    <div class="d-flex align-items-center justify-content-center gap-1.5" id="order-preparing-group-${orderId}">
                        <button type="button"
                                class="btn btn-sm btn-success rounded-pill px-2.5 py-1 font-size-12 fw-semibold d-inline-flex align-items-center gap-1"
                                title="Xác nhận đã nhận tiền và giao xong"
                                onclick="handleCompleteOrder(${orderId})">
                            <i class="bi bi-check-circle-fill"></i>
                            <span>Hoàn thành</span>
                        </button>
                        <button type="button"
                                class="btn btn-sm btn-outline-danger rounded-pill px-2.5 py-1 font-size-12 fw-semibold d-inline-flex align-items-center gap-1"
                                title="Khách từ chối nhận hàng"
                                onclick="openRejectDeliveryModal(${orderId}, '${result.data?.orderCode || ''}')">
                            <i class="bi bi-person-x"></i>
                            <span>Không nhận</span>
                        </button>
                        <a href="/merchant/orders/${orderId}"
                           class="btn btn-sm btn-light border rounded-pill px-2 py-1 font-size-12 text-secondary"
                           title="Xem chi tiết">
                            <i class="bi bi-eye"></i>
                        </a>
                    </div>
                `;
            }

            showToast('Thành công', 'Đã nhận đơn hàng! Đơn đã chuyển sang trạng thái Đang chuẩn bị.', 'success');
        } else {
            alert(result.message || 'Không thể nhận đơn hàng');
        }
    } catch (error) {
        console.error('Lỗi khi nhận đơn:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

// 2. MỞ MODAL HỦY ĐƠN (PENDING)
function openCancelModal(orderId, orderCode) {
    currentCancelOrderId = orderId;
    const codeEl = document.getElementById('modalOrderCode');
    if (codeEl) codeEl.innerText = orderCode;
    const reasonInput = document.getElementById('cancelReasonInput');
    if (reasonInput) reasonInput.value = '';
    if (cancelModalInstance) {
        cancelModalInstance.show();
    }
}

// 3. THỰC HIỆN HỦY ĐƠN
async function executeCancelOrder(orderId, reason) {
    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ cancelReason: reason })
        });

        const result = await response.json();
        if (result.success) {
            if (cancelModalInstance) {
                cancelModalInstance.hide();
            }

            const statusCol = document.getElementById(`order-status-col-${orderId}`);
            if (statusCol) {
                statusCol.innerHTML = `
                    <span class="badge badge-status bg-danger text-white">
                        Đã hủy
                    </span>
                    <div class="font-size-11 text-danger mt-1">
                        Lý do: ${reason || 'Merchant hủy đơn'}
                    </div>
                `;
            }

            const actionCol = document.getElementById(`order-action-col-${orderId}`);
            if (actionCol) {
                actionCol.innerHTML = `
                    <a href="/merchant/orders/${orderId}"
                       class="btn btn-sm btn-light border rounded-pill px-3 py-1 font-size-13 text-secondary">
                        <i class="bi bi-eye me-1"></i>Chi tiết
                    </a>
                `;
            }

            showToast('Đã hủy', 'Đơn hàng đã được hủy thành công.', 'warning');
        } else {
            alert(result.message || 'Không thể hủy đơn hàng');
        }
    } catch (error) {
        console.error('Lỗi khi hủy đơn:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

// 4. XỬ LÝ HOÀN THÀNH ĐƠN (ĐÃ NHẬN TIỀN & GIAO XONG)
async function handleCompleteOrder(orderId) {
    if (!confirm('Xác nhận bạn đã giao món và nhận tiền đầy đủ cho đơn hàng này?')) return;

    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.success) {
            const statusCol = document.getElementById(`order-status-col-${orderId}`);
            if (statusCol) {
                statusCol.innerHTML = `
                    <span class="badge badge-status bg-success text-white">
                        Hoàn thành
                    </span>
                `;
            }

            const actionCol = document.getElementById(`order-action-col-${orderId}`);
            if (actionCol) {
                actionCol.innerHTML = `
                    <a href="/merchant/orders/${orderId}"
                       class="btn btn-sm btn-light border rounded-pill px-3 py-1 font-size-13 text-secondary">
                        <i class="bi bi-eye me-1"></i>Chi tiết
                    </a>
                `;
            }

            showToast('Thành công', 'Đơn hàng đã được hoàn thành thành công!', 'success');
        } else {
            alert(result.message || 'Không thể hoàn thành đơn hàng');
        }
    } catch (error) {
        console.error('Lỗi khi hoàn thành đơn:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

// 5. MỞ MODAL KHÁCH KHÔNG NHẬN HÀNG
function openRejectDeliveryModal(orderId, orderCode) {
    currentRejectOrderId = orderId;
    const codeEl = document.getElementById('rejectModalOrderCode');
    if (codeEl) codeEl.innerText = orderCode;
    const reasonInput = document.getElementById('rejectReasonInput');
    if (reasonInput) reasonInput.value = 'Khách không nhận hàng';
    if (rejectModalInstance) {
        rejectModalInstance.show();
    }
}

// 6. THỰC HIỆN GHI NHẬN KHÁCH KHÔNG NHẬN HÀNG
async function executeRejectDelivery(orderId, reason) {
    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/failed-delivery`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ cancelReason: reason })
        });

        const result = await response.json();
        if (result.success) {
            if (rejectModalInstance) {
                rejectModalInstance.hide();
            }

            const statusCol = document.getElementById(`order-status-col-${orderId}`);
            if (statusCol) {
                statusCol.innerHTML = `
                    <span class="badge badge-status bg-danger text-white">
                        Đã hủy
                    </span>
                    <div class="font-size-11 text-danger mt-1">
                        Lý do: ${reason || 'Khách không nhận hàng'}
                    </div>
                `;
            }

            const actionCol = document.getElementById(`order-action-col-${orderId}`);
            if (actionCol) {
                actionCol.innerHTML = `
                    <a href="/merchant/orders/${orderId}"
                       class="btn btn-sm btn-light border rounded-pill px-3 py-1 font-size-13 text-secondary">
                        <i class="bi bi-eye me-1"></i>Chi tiết
                    </a>
                `;
            }

            showToast('Đã hủy', 'Đã ghi nhận đơn hàng bị hủy do khách không nhận hàng.', 'warning');
        } else {
            alert(result.message || 'Không thể ghi nhận hủy đơn hàng');
        }
    } catch (error) {
        console.error('Lỗi khi ghi nhận khách không nhận:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

function showToast(title, message, type = 'info') {
    if (typeof window.showMerchantToast === 'function') {
        window.showMerchantToast(title, message, type);
    } else {
        alert(message);
    }
}
