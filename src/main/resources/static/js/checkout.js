/**
 * Trang Thanh toán Đơn hàng (Checkout)
 * File: static/js/checkout.js
 *
 * Giỏ hàng lấy từ /api/cart. Giá hiển thị ở đây luôn là giá mới nhất trong
 * database, nên số tiền trên màn hình khớp chính xác với số tiền server tính
 * lúc tạo đơn.
 */

let selectedPayment = 'COD';
let appliedVoucher = '';
let discountAmount = 0;
let currentShippingFee = 15000;
let selectedDeliveryPartnerId = null;
let serviceFee = 0;
let subtotalPrice = 0;
let currentMerchantId = null;
let currentSelectedAddress = null;
let cartHasUnavailableItems = false;

// Các mã giảm giá server chấp nhận (khớp với UserOrderServiceImpl)
const VOUCHERS = {
    'GIAM10K': { type: 'amount', value: 10000, label: 'Giảm 10.000đ' },
    'GIAM20K': { type: 'amount', value: 20000, label: 'Giảm 20.000đ' },
    'GIAM50K': { type: 'amount', value: 50000, label: 'Giảm 50.000đ' },
    'GIAM10%': { type: 'percent', value: 10, label: 'Giảm 10% tiền món' },
    'GIAM10PT': { type: 'percent', value: 10, label: 'Giảm 10% tiền món' },
    'GIAM20%': { type: 'percent', value: 20, label: 'Giảm 20% tiền món' },
    'GIAM20PT': { type: 'percent', value: 20, label: 'Giảm 20% tiền món' },
    'GIAM50%': { type: 'percent', value: 50, label: 'Giảm 50% tiền món' },
    'GIAM50PT': { type: 'percent', value: 50, label: 'Giảm 50% tiền món' },
    'FREESHIP': { type: 'freeship', value: 0, label: 'Miễn phí vận chuyển' }
};

function formatCurrency(val) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(Number(val) || 0)) + ' đ';
}

// ==================== 1. CHỌN ĐỊA CHỈ ====================

function selectModalAddress(cardEl) {
    document.querySelectorAll('.address-modal-card').forEach(c => {
        c.classList.remove('selected', 'border-danger', 'bg-danger-subtle');
        c.classList.add('border-light-subtle', 'bg-white');
        const radio = c.querySelector('input[type="radio"]');
        if (radio) radio.checked = false;
    });

    cardEl.classList.add('selected', 'border-danger', 'bg-danger-subtle');
    cardEl.classList.remove('border-light-subtle', 'bg-white');

    const r = cardEl.querySelector('input[type="radio"]');
    if (r) r.checked = true;
}

function confirmAddressSelection() {
    const selectedModalCard = document.querySelector('.address-modal-card.selected');

    if (!selectedModalCard) {
        alert('Vui lòng chọn một địa chỉ nhận hàng.');
        return;
    }

    const id = selectedModalCard.getAttribute('data-id');
    const name = selectedModalCard.getAttribute('data-name') || '';
    const phone = selectedModalCard.getAttribute('data-phone') || '';
    const address = selectedModalCard.getAttribute('data-address') || '';
    const note = selectedModalCard.getAttribute('data-note') || '';
    const isDefault = selectedModalCard.getAttribute('data-default') === 'true';

    currentSelectedAddress = { id, name, phone, address, note, isDefault };

    const nameEl = document.getElementById('selectedContactName');
    if (nameEl) nameEl.innerText = name;

    const phoneEl = document.getElementById('selectedContactPhone');
    if (phoneEl) phoneEl.innerText = phone;

    const addrEl = document.getElementById('selectedFullAddress');
    if (addrEl) addrEl.innerText = address;

    const defaultBadge = document.getElementById('selectedDefaultBadge');
    if (defaultBadge) {
        defaultBadge.style.display = isDefault ? 'inline-block' : 'none';
    }

    const noteBox = document.getElementById('selectedNoteContainer');
    const noteText = document.getElementById('selectedNoteText');
    if (noteBox && noteText) {
        if (note && note.trim().length > 0) {
            noteText.innerText = note;
            noteBox.style.display = 'block';
        } else {
            noteBox.style.display = 'none';
        }
    }

    const modalEl = document.getElementById('chooseAddressModal');
    if (modalEl && typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    }

    if (currentMerchantId && id) {
        loadShippingQuotes(currentMerchantId, id);
    }
}

// ==================== 2. BÁO GIÁ VẬN CHUYỂN ====================

async function loadShippingQuotes(merchantId, addressId) {
    const container = document.getElementById('deliveryPartnersContainer');

    if (!container) return;

    container.innerHTML = `
        <div class="text-center py-3 text-muted font-size-13">
            <span class="spinner-border spinner-border-sm text-danger me-2"></span>
            Đang tính phí vận chuyển theo khoảng cách...
        </div>
    `;

    try {
        const fetchFn = (typeof fetchWithAuth === 'function') ? fetchWithAuth : fetch;
        const response = await fetchFn(
            `/api/delivery/quotes?merchantId=${merchantId}&addressId=${addressId}`
        );

        if (response.ok) {
            renderShippingQuotes(await response.json());
        } else {
            renderFallbackDeliveryPartner();
        }
    } catch (e) {
        console.warn('Không tải được báo giá vận chuyển:', e);
        renderFallbackDeliveryPartner();
    }
}

function renderShippingQuotes(quotes) {
    const container = document.getElementById('deliveryPartnersContainer');
    const distanceBadge = document.getElementById('deliveryDistanceText');

    if (!container) return;

    if (!quotes || quotes.length === 0) {
        renderFallbackDeliveryPartner();
        return;
    }

    const dist = quotes[0].distanceKm || 3.0;
    if (distanceBadge) {
        distanceBadge.innerText = `Khoảng cách: ~${dist} km`;
    }

    container.innerHTML = '';

    quotes.forEach((q, index) => {
        const isSelected = index === 0;

        if (isSelected) {
            selectedDeliveryPartnerId = q.partnerId;
            currentShippingFee = q.shippingFee;
        }

        const card = document.createElement('div');
        card.className = `delivery-partner-card d-flex justify-content-between align-items-center ${isSelected ? 'selected' : ''}`;
        card.setAttribute('data-partner-id', q.partnerId);
        card.setAttribute('data-fee', q.shippingFee);
        card.onclick = () => selectDeliveryPartnerCard(card, q.partnerId, q.shippingFee);

        const peakBadge = q.peakHour
            ? `<span class="badge bg-warning-subtle text-warning border border-warning-subtle font-size-11 ms-1.5"><i class="bi bi-lightning-charge-fill me-0.5"></i>Giờ cao điểm</span>`
            : `<span class="badge bg-success-subtle text-success border border-success-subtle font-size-11 ms-1.5"><i class="bi bi-check-circle me-0.5"></i>Tiêu chuẩn</span>`;

        card.innerHTML = `
            <div class="d-flex align-items-center gap-3">
                <div class="bg-light p-2 rounded-3 text-danger fs-4 d-flex align-items-center justify-content-center" style="width: 44px; height: 44px;">
                    <i class="bi bi-truck"></i>
                </div>
                <div>
                    <div class="d-flex align-items-center flex-wrap">
                        <strong class="text-dark fs-6">${q.partnerName}</strong>
                        ${peakBadge}
                    </div>
                    <small class="text-muted d-block mt-0.5 font-size-12">
                        <i class="bi bi-clock me-1"></i>Giao trong khoảng ${Math.max(0.5, Math.round(q.distanceKm * 0.5))} phút &bull; ~${q.distanceKm} km
                    </small>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <strong class="text-danger fs-6">${formatCurrency(q.shippingFee)}</strong>
                <input class="form-check-input" type="radio" name="deliveryPartnerRadio" value="${q.partnerId}" ${isSelected ? 'checked' : ''}>
            </div>
        `;

        container.appendChild(card);
    });

    recalculateVoucher();
    updatePriceSummary();
}

function renderFallbackDeliveryPartner() {
    const container = document.getElementById('deliveryPartnersContainer');
    const distanceBadge = document.getElementById('deliveryDistanceText');

    if (distanceBadge) distanceBadge.innerText = 'Khoảng cách: ~3.0 km';
    if (!container) return;

    currentShippingFee = 15000;
    selectedDeliveryPartnerId = null;

    container.innerHTML = `
        <div class="delivery-partner-card selected d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-3">
                <div class="bg-light p-2 rounded-3 text-danger fs-4 d-flex align-items-center justify-content-center" style="width: 44px; height: 44px;">
                    <i class="bi bi-truck"></i>
                </div>
                <div>
                    <strong class="text-dark fs-6">Giao hàng tiêu chuẩn MealChoice</strong>
                    <small class="text-muted d-block mt-0.5 font-size-12">
                        <i class="bi bi-clock me-1"></i>Giao trong khoảng 12 phút
                    </small>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <strong class="text-danger fs-6">15.000 đ</strong>
                <input class="form-check-input" type="radio" name="deliveryPartnerRadio" checked>
            </div>
        </div>
    `;

    recalculateVoucher();
    updatePriceSummary();
}

function selectDeliveryPartnerCard(cardEl, partnerId, fee) {
    document.querySelectorAll('.delivery-partner-card').forEach(c => {
        c.classList.remove('selected');
        const radio = c.querySelector('input[type="radio"]');
        if (radio) radio.checked = false;
    });

    cardEl.classList.add('selected');
    const r = cardEl.querySelector('input[type="radio"]');
    if (r) r.checked = true;

    selectedDeliveryPartnerId = partnerId;
    currentShippingFee = fee;

    recalculateVoucher();
    updatePriceSummary();
}

// ==================== 3. PHƯƠNG THỨC THANH TOÁN ====================

function selectPaymentMethod(cardEl, method) {
    if (cardEl.classList.contains('disabled-payment-method')) {
        return;
    }

    selectedPayment = method;

    document.querySelectorAll('.payment-card').forEach(c => {
        c.classList.remove('selected');
        const radio = c.querySelector('input[type="radio"]');
        if (radio) radio.checked = false;
    });

    cardEl.classList.add('selected');
    const r = cardEl.querySelector('input[type="radio"]');
    if (r) r.checked = true;

    const bankBox = document.getElementById('bankTransferInfoBox');
    if (bankBox) {
        bankBox.style.display = (method === 'CARD') ? 'block' : 'none';
    }
}

function copyBankAccount(event) {
    if (event) event.stopPropagation();

    const acc = document.getElementById('checkoutMerchantBankAccount')?.innerText;
    if (!acc) return;

    navigator.clipboard.writeText(acc)
        .then(() => alert('Đã sao chép số tài khoản: ' + acc))
        .catch(() => alert('Số tài khoản: ' + acc));
}

function updateMerchantBankInfo(summary) {
    const bankName = (summary.merchantBankName || '').trim();
    const bankAccount = (summary.merchantBankAccountNumber || '').trim();
    const hasBank = bankName.length > 0 && bankAccount.length > 0;

    const cardEl = document.getElementById('paymentMethodCARD');
    const cardRadio = document.getElementById('paymentRadioCARD');
    const notSupportedNotice = document.getElementById('bankNotSupportedNotice');
    const bankBox = document.getElementById('bankTransferInfoBox');

    if (!hasBank) {
        // Quán chưa khai báo tài khoản ngân hàng: chỉ cho thanh toán COD
        cardEl?.classList.add('disabled-payment-method');
        if (cardRadio) cardRadio.disabled = true;
        if (notSupportedNotice) notSupportedNotice.style.display = 'block';
        if (bankBox) bankBox.style.display = 'none';

        if (selectedPayment === 'CARD') {
            const codCard = document.getElementById('paymentMethodCOD');
            if (codCard) selectPaymentMethod(codCard, 'COD');
        }
        return;
    }

    cardEl?.classList.remove('disabled-payment-method');
    if (cardRadio) cardRadio.disabled = false;
    if (notSupportedNotice) notSupportedNotice.style.display = 'none';

    const bankNameEl = document.getElementById('checkoutMerchantBankName');
    if (bankNameEl) bankNameEl.innerText = bankName;

    const bankAccEl = document.getElementById('checkoutMerchantBankAccount');
    if (bankAccEl) bankAccEl.innerText = bankAccount;

    const ownerEl = document.getElementById('checkoutMerchantOwnerName');
    if (ownerEl) ownerEl.innerText = summary.merchantName || 'Cửa hàng';

    if (selectedPayment === 'CARD' && bankBox) {
        bankBox.style.display = 'block';
    }
}

// ==================== 4. TẢI GIỎ HÀNG TỪ SERVER ====================

async function loadCheckoutCart() {
    const itemsList = document.getElementById('checkoutItemsList');
    const itemCount = document.getElementById('checkoutItemCount');
    const merchantTitle = document.getElementById('checkoutMerchantName');

    if (!itemsList) return;

    itemsList.innerHTML = `
        <div class="text-center py-4 text-muted font-size-13">
            <span class="spinner-border spinner-border-sm text-danger me-2"></span>
            Đang tải giỏ hàng...
        </div>
    `;

    const summary = (typeof refreshCart === 'function')
        ? await refreshCart()
        : null;

    const urlParams = new URLSearchParams(window.location.search);
    const targetMerchantId = urlParams.get('merchantId');

    if (summary && summary.items && targetMerchantId) {
        summary.items = summary.items.filter(item => String(item.merchantId) === targetMerchantId);
        
        // Recalculate summary totals
        summary.subtotalPrice = summary.items.reduce((sum, item) => sum + Number(item.subtotal || 0), 0);
        summary.serviceFee = summary.items.reduce((max, item) => Math.max(max, Number(item.serviceFee || 0)), 0);
        summary.totalItems = summary.items.reduce((sum, item) => sum + Number(item.quantity || 1), 0);
        summary.hasUnavailableItems = summary.items.some(item => !item.available);
        summary.empty = summary.items.length === 0;
        
        if (summary.items.length > 0) {
            summary.merchantName = summary.items[0].merchantName;
            summary.merchantId = targetMerchantId;
        }
    }

    if (!summary || summary.empty) {
        itemsList.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="bi bi-cart-x fs-2 d-block text-secondary opacity-50 mb-2"></i>
                <p class="mb-2 fw-medium">Không có món ăn nào của quán này trong giỏ hàng</p>
                <a href="/cart" class="btn btn-outline-danger btn-sm rounded-pill px-3">Quay lại giỏ hàng</a>
            </div>
        `;

        if (itemCount) itemCount.innerText = '0 món';

        subtotalPrice = 0;
        serviceFee = 0;
        cartHasUnavailableItems = false;

        updatePriceSummary();
        return;
    }

    currentMerchantId = summary.merchantId;
    cartHasUnavailableItems = !!summary.hasUnavailableItems;

    if (merchantTitle) {
        merchantTitle.innerText = summary.merchantName || 'Cửa hàng';
    }

    updateMerchantBankInfo(summary);

    // Render danh sách món (nhóm theo quán)
    itemsList.innerHTML = '';

    const groups = new Map();
    summary.items.forEach(item => {
        const key = item.merchantId || 'unknown';
        if (!groups.has(key)) {
            groups.set(key, { merchantName: item.merchantName || 'Cửa hàng', items: [] });
        }
        groups.get(key).items.push(item);
    });

    groups.forEach((group, merchantId) => {
        if (groups.size > 1) {
            const header = document.createElement('div');
            header.className = 'fw-bold text-danger font-size-13 py-2 mt-2 border-bottom';
            header.innerHTML = `<i class="bi bi-shop me-1"></i>${group.merchantName}`;
            itemsList.appendChild(header);
        }

        group.items.forEach(item => {
            const div = document.createElement('div');
            div.className = 'd-flex justify-content-between align-items-center py-2.5 border-bottom border-light';

            const warning = item.available
                ? ''
                : `<div class="text-danger font-size-11 mt-0.5">
                       <i class="bi bi-exclamation-triangle me-1"></i>${item.unavailableReason || 'Không còn phục vụ'}
                   </div>`;

            const noteLine = item.note
                ? `<div class="text-muted font-size-11 fst-italic mt-0.5">${item.note}</div>`
                : '';

            div.innerHTML = `
                <div class="d-flex align-items-center gap-2.5 overflow-hidden">
                    <img src="${item.foodImage || '/images/default-food.svg'}"
                         class="food-item-img border"
                         alt="${item.foodName}"
                         data-fallback="/images/default-food.svg">
                    <div class="fw-bold text-dark fs-7">${item.quantity}x</div>
                    <div class="overflow-hidden">
                        <div class="fw-semibold text-dark fs-7 text-truncate" title="${item.foodName}">${item.foodName}</div>
                        <small class="text-muted font-size-12">${formatCurrency(item.effectivePrice)}</small>
                        ${noteLine}
                        ${warning}
                    </div>
                </div>
                <div class="fw-bold text-dark font-size-14 text-nowrap">${formatCurrency(item.subtotal)}</div>
            `;

            itemsList.appendChild(div);
        });
    });

    if (itemCount) {
        itemCount.innerText = `${summary.totalItems} món`;
    }

    subtotalPrice = Number(summary.subtotalPrice || 0);
    serviceFee = Number(summary.serviceFee || 0);

    // Có món hết phục vụ thì chặn đặt hàng
    const placeOrderBtn = document.getElementById('btnPlaceOrder');
    if (placeOrderBtn) {
        if (cartHasUnavailableItems) {
            placeOrderBtn.disabled = true;
            placeOrderBtn.innerHTML =
                '<i class="bi bi-exclamation-triangle me-2"></i>Bỏ món hết phục vụ để tiếp tục';
        } else {
            placeOrderBtn.disabled = false;
            placeOrderBtn.innerHTML = '<i class="bi bi-bag-check-fill me-2"></i>Đặt hàng';
        }
    }

    if (currentSelectedAddress && currentSelectedAddress.id && currentMerchantId) {
        loadShippingQuotes(currentMerchantId, currentSelectedAddress.id);
    }

    recalculateVoucher();
    updatePriceSummary();
}

// ==================== 5. VOUCHER & TỔNG TIỀN ====================

function recalculateVoucher() {
    if (!appliedVoucher) {
        discountAmount = 0;
        return;
    }

    const voucher = VOUCHERS[appliedVoucher];

    if (!voucher) {
        discountAmount = 0;
        return;
    }

    if (voucher.type === 'amount') {
        discountAmount = voucher.value;
    } else if (voucher.type === 'percent') {
        discountAmount = Math.round(subtotalPrice * voucher.value / 100);
    } else if (voucher.type === 'freeship') {
        discountAmount = currentShippingFee;
    }

    const ceiling = subtotalPrice + currentShippingFee;
    if (discountAmount > ceiling) {
        discountAmount = ceiling;
    }
}

function updatePriceSummary() {
    const total = subtotalPrice + currentShippingFee + serviceFee - discountAmount;

    const subEl = document.getElementById('summarySubtotal');
    if (subEl) subEl.innerText = formatCurrency(subtotalPrice);

    const shipEl = document.getElementById('summaryShippingFee');
    if (shipEl) shipEl.innerText = formatCurrency(currentShippingFee);

    const srvEl = document.getElementById('summaryServiceFee');
    if (srvEl) srvEl.innerText = formatCurrency(serviceFee);

    const totalEl = document.getElementById('summaryTotalAmount');
    if (totalEl) totalEl.innerText = formatCurrency(Math.max(0, total));

    const discRow = document.getElementById('summaryDiscountRow');
    if (discRow) {
        if (discountAmount > 0) {
            discRow.style.setProperty('display', 'flex', 'important');
            const discEl = document.getElementById('summaryDiscount');
            if (discEl) discEl.innerText = `- ${formatCurrency(discountAmount)}`;
        } else {
            discRow.style.setProperty('display', 'none', 'important');
        }
    }
}

// ==================== 6. KHỞI TẠO TRANG ====================

document.addEventListener('DOMContentLoaded', () => {

    // Địa chỉ đang chọn
    const initialSelectedCard =
        document.querySelector('.address-modal-card.selected') ||
        document.querySelector('.address-modal-card');

    if (initialSelectedCard) {
        currentSelectedAddress = {
            id: initialSelectedCard.getAttribute('data-id'),
            name: initialSelectedCard.getAttribute('data-name') || '',
            phone: initialSelectedCard.getAttribute('data-phone') || '',
            address: initialSelectedCard.getAttribute('data-address') || '',
            note: initialSelectedCard.getAttribute('data-note') || '',
            isDefault: initialSelectedCard.getAttribute('data-default') === 'true'
        };
    }

    loadCheckoutCart();

    // ---------- Áp dụng voucher ----------
    document.getElementById('btnApplyVoucher')?.addEventListener('click', () => {
        const codeInput = document.getElementById('voucherInput');
        const code = codeInput ? codeInput.value.trim().toUpperCase() : '';
        const msgEl = document.getElementById('voucherMessage');

        if (!code) {
            appliedVoucher = '';
            discountAmount = 0;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-danger';
                msgEl.innerText = 'Nhập mã giảm giá trước khi áp dụng';
            }
            updatePriceSummary();
            return;
        }

        const voucher = VOUCHERS[code];

        if (!voucher) {
            appliedVoucher = '';
            discountAmount = 0;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-danger';
                msgEl.innerText = 'Mã này không dùng được. Kiểm tra lại hoặc thử mã khác.';
            }
            updatePriceSummary();
            return;
        }

        appliedVoucher = code;
        recalculateVoucher();

        if (msgEl) {
            msgEl.className = 'font-size-12 mt-1 text-success';
            msgEl.innerText = `Đã áp dụng ${code}: ${voucher.label}`;
        }

        updatePriceSummary();
    });

    // ---------- Lưu địa chỉ mới ----------
    document.getElementById('btnSaveAddress')?.addEventListener('click', async () => {
        const contactName = document.getElementById('modalContactName')?.value.trim();
        const contactPhone = document.getElementById('modalContactPhone')?.value.trim();
        const city = document.getElementById('modalCity')?.value.trim();
        const district = document.getElementById('modalDistrict')?.value.trim();
        const ward = document.getElementById('modalWard')?.value.trim();
        const street = document.getElementById('modalStreet')?.value.trim();
        const note = document.getElementById('modalNote')?.value.trim();
        const isDefault = document.getElementById('modalIsDefault')?.checked ?? true;

        if (!contactName || !contactPhone || !city || !district || !ward || !street) {
            alert('Điền đầy đủ các ô có dấu * trước khi lưu.');
            return;
        }

        if (!/^(0|\+84)[0-9]{9,10}$/.test(contactPhone)) {
            alert('Số điện thoại chưa đúng định dạng. Ví dụ: 0987654321');
            return;
        }

        const saveBtn = document.getElementById('btnSaveAddress');
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...';
        }

        try {
            const fetchFn = (typeof fetchWithAuth === 'function') ? fetchWithAuth : fetch;
            const response = await fetchFn('/api/user/address', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(typeof getAuthHeaders === 'function' ? getAuthHeaders() : {})
                },
                body: JSON.stringify({
                    contactName, contactPhone, city, district, ward, street, note, isDefault
                })
            });

            if (response.ok) {
                location.reload();
            } else {
                const err = await response.json().catch(() => ({}));
                alert(err.message || 'Không lưu được địa chỉ.');
                if (saveBtn) {
                    saveBtn.disabled = false;
                    saveBtn.innerHTML = 'Lưu địa chỉ';
                }
            }
        } catch (e) {
            console.error('Lỗi lưu địa chỉ:', e);
            alert('Không kết nối được tới máy chủ.');
            if (saveBtn) {
                saveBtn.disabled = false;
                saveBtn.innerHTML = 'Lưu địa chỉ';
            }
        }
    });

    // ---------- Đặt hàng ----------
    document.getElementById('btnPlaceOrder')?.addEventListener('click', async () => {

        if (!currentSelectedAddress || !currentSelectedAddress.address) {
            alert('Chọn địa chỉ giao hàng trước khi đặt món.');
            return;
        }

        const summary = (typeof getCartSummary === 'function') ? getCartSummary() : null;

        if (!summary || summary.empty) {
            alert('Giỏ hàng đang trống.');
            return;
        }

        if (summary.hasUnavailableItems) {
            alert('Giỏ hàng có món không còn phục vụ. Bỏ món đó ra trước khi đặt.');
            return;
        }

        const urlParams = new URLSearchParams(window.location.search);
        const targetMerchantId = urlParams.get('merchantId');

        const payload = {
            merchantId: targetMerchantId,
            deliveryPartnerId: selectedDeliveryPartnerId,
            contactName: currentSelectedAddress.name,
            contactPhone: currentSelectedAddress.phone,
            deliveryAddress: currentSelectedAddress.address,
            note: document.getElementById('orderNoteInput')?.value.trim() || '',
            paymentMethod: selectedPayment,
            voucherCode: appliedVoucher
        };

        const btn = document.getElementById('btnPlaceOrder');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...';
        }

        try {
            const fetchFn = (typeof fetchWithAuth === 'function') ? fetchWithAuth : fetch;
            const response = await fetchFn('/api/checkout/place-order', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(typeof getAuthHeaders === 'function' ? getAuthHeaders() : {})
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json().catch(() => ({}));

            if (result.success) {
                // Giỏ hàng đã được dọn một phần, đồng bộ lại
                if (typeof refreshCart === 'function') {
                    await refreshCart();
                }

                window.location.href = `/checkout/success?orderCode=${result.orderCode}`;
            } else {
                alert(result.message || 'Đặt hàng chưa thành công. Vui lòng thử lại.');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-bag-check-fill me-2"></i>Đặt hàng';
                }
            }
        } catch (e) {
            console.error('Lỗi đặt hàng:', e);
            alert('Không kết nối được tới máy chủ. Kiểm tra mạng và thử lại.');
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-bag-check-fill me-2"></i>Đặt hàng';
            }
        }
    });
});