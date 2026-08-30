/**
 * Quản lý Danh sách Đơn hàng của Merchant
 * File: static/js/merchant-order-list.js
 */

let currentCancelOrderId = null;
let cancelModalInstance = null;
let currentRejectOrderId = null;
let rejectModalInstance = null;

// Quản lý các bộ đếm thời gian
const activeTimers = new Map();

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

    // Khởi tạo các bộ đếm ngược trên trang
    initAllTimers();
});

// ==================== TIMER MANAGEMENT ====================

function initAllTimers() {
    // 1. Quét các đơn đang chuẩn bị (PREPARING)
    document.querySelectorAll('.prep-timer-badge').forEach(el => {
        const orderId = el.dataset.orderId;
        const remaining = parseInt(el.dataset.remaining, 10);
        if (orderId && !isNaN(remaining) && remaining > 0) {
            startPrepCountdown(orderId, remaining);
        }
    });

    // 2. Quét các đơn đang giao (DELIVERING)
    document.querySelectorAll('.delivery-timer-badge').forEach(el => {
        const orderId = el.dataset.orderId;
        const remaining = parseInt(el.dataset.remaining, 10);
        if (orderId && !isNaN(remaining)) {
            startDeliveryCountdown(orderId, remaining);
        }
    });
}

function formatTime(seconds) {
    if (seconds <= 0) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
}

// Đếm ngược thời gian chuẩn bị món
function startPrepCountdown(orderId, initialSeconds) {
    if (activeTimers.has(`prep_${orderId}`)) {
        clearInterval(activeTimers.get(`prep_${orderId}`));
    }

    let remaining = initialSeconds;
    const badgeEl = document.getElementById(`prep-timer-${orderId}`);

    function updateDisplay() {
        if (badgeEl) {
            const textEl = badgeEl.querySelector('.timer-text');
            if (textEl) {
                textEl.innerText = `Chuẩn bị: ${formatTime(remaining)}`;
            }
        }
    }

    updateDisplay();

    const intervalId = setInterval(() => {
        remaining--;
        if (remaining <= 0) {
            clearInterval(intervalId);
            activeTimers.delete(`prep_${orderId}`);
            // Hết giờ chuẩn bị -> Tự động chuyển sang trạng thái Đang giao hàng
            transitionToDelivering(orderId, 15 * 60); // Mặc định 15 phút giao
        } else {
            updateDisplay();
        }
    }, 1000);

    activeTimers.set(`prep_${orderId}`, intervalId);
}

// Đếm ngược thời gian giao hàng
function startDeliveryCountdown(orderId, initialSeconds) {
    if (activeTimers.has(`delivery_${orderId}`)) {
        clearInterval(activeTimers.get(`delivery_${orderId}`));
    }

    let remaining = initialSeconds;
    const badgeEl = document.getElementById(`delivery-timer-${orderId}`);
    const completeBtn = document.getElementById(`btn-complete-${orderId}`);

    function updateDisplay() {
        if (badgeEl) {
            const textEl = badgeEl.querySelector('.timer-text');
            if (textEl) {
                textEl.innerText = `Giao hàng: ${formatTime(remaining)}`;
            }
        }
        if (completeBtn && remaining > 0) {
            const btnText = completeBtn.querySelector('.btn-complete-text');
            if (btnText) btnText.innerText = `Đang giao (${formatTime(remaining)})`;
        }
    }

    if (remaining > 0) {
        updateDisplay();
        const intervalId = setInterval(() => {
            remaining--;
            if (remaining <= 0) {
                clearInterval(intervalId);
                activeTimers.delete(`delivery_${orderId}`);
                enableCompleteButton(orderId);
            } else {
                updateDisplay();
            }
        }, 1000);

        activeTimers.set(`delivery_${orderId}`, intervalId);
    } else {
        enableCompleteButton(orderId);
    }
}

// Kích hoạt nút Hoàn thành & Không nhận khi hết thời gian giao
function enableCompleteButton(orderId) {
    const badgeEl = document.getElementById(`delivery-timer-${orderId}`);
    if (badgeEl) {
        badgeEl.innerHTML = `<i class="bi bi-geo-alt-fill text-success me-1"></i><span class="text-success fw-bold">Đã đến nơi</span>`;
    }

    const completeBtn = document.getElementById(`btn-complete-${orderId}`);
    if (completeBtn) {
        completeBtn.disabled = false;
        completeBtn.classList.remove('btn-outline-success', 'opacity-75');
        completeBtn.classList.add('btn-success');
        completeBtn.title = 'Xác nhận đã nhận tiền và giao xong';
        const btnText = completeBtn.querySelector('.btn-complete-text');
        if (btnText) btnText.innerText = 'Hoàn thành';
    }

    const rejectBtn = document.getElementById(`btn-reject-${orderId}`);
    if (rejectBtn) {
        rejectBtn.classList.remove('d-none');
    }
}

// Chuyển giao diện đơn hàng sang DELIVERING
function transitionToDelivering(orderId, deliverySeconds = 900) {
    // 1. Cập nhật cột trạng thái
    const statusCol = document.getElementById(`order-status-col-${orderId}`);
    if (statusCol) {
        statusCol.innerHTML = `
            <span class="badge badge-status bg-info text-dark">
                Đang giao
            </span>
            <div class="font-size-11 text-info fw-medium mt-1 delivery-timer-badge"
                 id="delivery-timer-${orderId}"
                 data-remaining="${deliverySeconds}"
                 data-order-id="${orderId}">
                <i class="bi bi-truck me-1"></i><span class="timer-text">Đang giao...</span>
            </div>
        `;
    }

    // 2. Cập nhật cột hành động sang nhóm DELIVERING (nút Hoàn thành disabled, nút Không nhận ẩn)
    const actionCol = document.getElementById(`order-action-col-${orderId}`);
    if (actionCol) {
        actionCol.innerHTML = `
            <div class="d-flex align-items-center justify-content-center gap-1.5" id="order-delivering-group-${orderId}">
                <button type="button"
                        class="btn btn-sm btn-outline-success opacity-75 rounded-pill px-2.5 py-1 font-size-12 fw-semibold d-inline-flex align-items-center gap-1 complete-btn"
                        id="btn-complete-${orderId}"
                        disabled
                        title="Đang giao hàng, vui lòng chờ..."
                        data-order-id="${orderId}"
                        onclick="handleCompleteOrder(${orderId})">
                    <i class="bi bi-check-circle-fill"></i>
                    <span class="btn-complete-text">Đang giao...</span>
                </button>
                <button type="button"
                        class="btn btn-sm btn-outline-danger rounded-pill px-2.5 py-1 font-size-12 fw-semibold d-inline-flex align-items-center gap-1 d-none"
                        id="btn-reject-${orderId}"
                        title="Khách từ chối nhận hàng"
                        onclick="openRejectDeliveryModal(${orderId}, '')">
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

    // Bắt đầu đếm ngược giao hàng
    startDeliveryCountdown(orderId, deliverySeconds);
}

// ==================== ACTIONS ====================

// 1. XỬ LÝ NHẬN ĐƠN (ACCEPT ORDER -> PREPARING)
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
            const data = result.data;
            const prepSeconds = data?.remainingPrepSeconds || 600; // 10 phút mặc định

            // Cập nhật trạng thái hiển thị
            const statusCol = document.getElementById(`order-status-col-${orderId}`);
            if (statusCol) {
                statusCol.innerHTML = `
                    <span class="badge badge-status bg-primary text-white">
                        Đang chuẩn bị
                    </span>
                    <div class="font-size-11 text-primary fw-medium mt-1 prep-timer-badge"
                         id="prep-timer-${orderId}"
                         data-remaining="${prepSeconds}"
                         data-order-id="${orderId}">
                        <i class="bi bi-stopwatch me-1"></i><span class="timer-text">Chuẩn bị món...</span>
                    </div>
                `;
            }

            // Cập nhật các nút hành động sang nhóm PREPARING (Chỉ có nút Giao ngay + Chi tiết)
            const actionCol = document.getElementById(`order-action-col-${orderId}`);
            if (actionCol) {
                actionCol.innerHTML = `
                    <div class="d-flex align-items-center justify-content-center gap-1.5" id="order-preparing-group-${orderId}">
                        <button type="button"
                                class="btn btn-sm btn-primary rounded-pill px-2.5 py-1 font-size-12 fw-semibold d-inline-flex align-items-center gap-1"
                                title="Món đã chuẩn bị xong, bắt đầu giao ngay"
                                onclick="handleStartDelivery(${orderId})">
                            <i class="bi bi-send-fill"></i>
                            <span>Giao hàng ngay</span>
                        </button>
                        <a href="/merchant/orders/${orderId}"
                           class="btn btn-sm btn-light border rounded-pill px-2 py-1 font-size-12 text-secondary"
                           title="Xem chi tiết">
                            <i class="bi bi-eye"></i>
                        </a>
                    </div>
                `;
            }

            // Khởi động đếm ngược chuẩn bị
            startPrepCountdown(orderId, prepSeconds);

            showToast('Thành công', 'Đã nhận đơn hàng! Đơn đã chuyển sang trạng thái Đang chuẩn bị.', 'success');
        } else {
            alert(result.message || 'Không thể nhận đơn hàng');
        }
    } catch (error) {
        console.error('Lỗi khi nhận đơn:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

// 2. XỬ LÝ BẮT ĐẦU GIAO HÀNG (START DELIVERY -> DELIVERING)
async function handleStartDelivery(orderId) {
    if (!confirm('Xác nhận món ăn đã làm xong và bắt đầu giao cho khách?')) return;

    try {
        const response = await fetch(`/api/merchant/orders/${orderId}/start-delivery`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.success) {
            const data = result.data;
            const deliverySeconds = data?.remainingDeliverySeconds || 900;

            // Xóa timer prep nếu có
            if (activeTimers.has(`prep_${orderId}`)) {
                clearInterval(activeTimers.get(`prep_${orderId}`));
                activeTimers.delete(`prep_${orderId}`);
            }

            transitionToDelivering(orderId, deliverySeconds);
            showToast('Bắt đầu giao', 'Đơn hàng đã chuyển sang trạng thái Đang giao!', 'info');
        } else {
            alert(result.message || 'Không thể cập nhật trạng thái giao hàng');
        }
    } catch (error) {
        console.error('Lỗi khi bắt đầu giao hàng:', error);
        alert('Đã xảy ra lỗi kết nối');
    }
}

// 3. MỞ MODAL HỦY ĐƠN (PENDING)
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

// 4. THỰC HIỆN HỦY ĐƠN
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

// 5. XỬ LÝ HOÀN THÀNH ĐƠN (ĐÃ NHẬN TIỀN & GIAO XONG)
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
            // Xóa timer nếu có
            if (activeTimers.has(`delivery_${orderId}`)) {
                clearInterval(activeTimers.get(`delivery_${orderId}`));
                activeTimers.delete(`delivery_${orderId}`);
            }

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

// 6. MỞ MODAL KHÁCH KHÔNG NHẬN HÀNG
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

// 7. THỰC HIỆN GHI NHẬN KHÁCH KHÔNG NHẬN HÀNG
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

            // Xóa timer nếu có
            if (activeTimers.has(`delivery_${orderId}`)) {
                clearInterval(activeTimers.get(`delivery_${orderId}`));
                activeTimers.delete(`delivery_${orderId}`);
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
