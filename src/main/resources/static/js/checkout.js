/**
 * Quản lý Trang Thanh toán Đơn hàng (Checkout)
 * File: static/js/checkout.js
 */

let selectedPayment = 'COD';
let appliedVoucher = '';
let discountAmount = 0;
let currentShippingFee = 15000;
let selectedDeliveryPartnerId = null;
let shippingQuotesList = [];
let serviceFee = 0;
let subtotalPrice = 0;
let currentMerchantId = null;
let merchantBankInfo = null;
let currentSelectedAddress = null;

function formatCurrency(val) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(val)) + ' đ';
}

// 1. CHỌN ĐỊA CHỈ TRONG MODAL
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

// Xác nhận chọn địa chỉ từ modal
function confirmAddressSelection() {
    const selectedModalCard = document.querySelector('.address-modal-card.selected');
    if (!selectedModalCard) {
        alert('Vui lòng chọn một địa chỉ nhận hàng!');
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

    // Đóng modal chọn địa chỉ
    const modalEl = document.getElementById('chooseAddressModal');
    if (modalEl && typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    }

    // Tự động tính toán lại báo giá giao hàng theo địa chỉ mới
    if (currentMerchantId && id) {
        loadShippingQuotes(currentMerchantId, id);
    }
}

// 2. TÍNH TOÁN VÀ RENDER BÁO GIÁ ĐƠN VỊ VẬN CHUYỂN
async function loadShippingQuotes(merchantId, addressId) {
    const container = document.getElementById('deliveryPartnersContainer');
    const distanceBadge = document.getElementById('deliveryDistanceText');

    if (!container) return;

    container.innerHTML = `
        <div class="text-center py-3 text-muted font-size-13">
            <span class="spinner-border spinner-border-sm text-danger me-2"></span>
            Đang tính phí vận chuyển theo khoảng cách...
        </div>
    `;

    try {
        const fetchFn = (typeof fetchWithAuth === 'function') ? fetchWithAuth : fetch;
        const response = await fetchFn(`/api/delivery/quotes?merchantId=${merchantId}&addressId=${addressId}`, {
            headers: (typeof getAuthHeaders === 'function') ? getAuthHeaders() : {}
        });

        if (response.ok) {
            const quotes = await response.json();
            shippingQuotesList = quotes;
            renderShippingQuotes(quotes);
        } else {
            renderFallbackDeliveryPartner();
        }
    } catch (e) {
        console.warn('Không thể tải quotes vận chuyển:', e);
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

    // Hiển thị khoảng cách ước tính
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
                        <i class="bi bi-clock me-1"></i>Giao dự kiến ~${Math.max(4, Math.round(q.distanceKm * 4))} phút di chuyển (4 phút/km) &bull; ~${q.distanceKm} km
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
                    <strong class="text-dark fs-6">Giao hàng Tiêu chuẩn MealChoice</strong>
                    <small class="text-muted d-block mt-0.5 font-size-12">
                        <i class="bi bi-clock me-1"></i>Giao nhanh dự kiến ~12 phút di chuyển (4 phút/km)
                    </small>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <strong class="text-danger fs-6">15.000 đ</strong>
                <input class="form-check-input" type="radio" name="deliveryPartnerRadio" checked>
            </div>
        </div>
    `;

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
    updatePriceSummary();
}

// 3. CHỌN PHƯƠNG THỨC THANH TOÁN
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
        if (method === 'CARD') {
            bankBox.style.display = 'block';
        } else {
            bankBox.style.display = 'none';
        }
    }
}

function copyBankAccount(event) {
    if (event) event.stopPropagation();
    const acc = document.getElementById('checkoutMerchantBankAccount')?.innerText;
    if (acc) {
        navigator.clipboard.writeText(acc).then(() => {
            alert('Đã sao chép số tài khoản: ' + acc);
        }).catch(() => {
            alert('Số tài khoản: ' + acc);
        });
    }
}

function updateMerchantBankStatus(data) {
    const bankName = data.bankName ? data.bankName.trim() : "";
    const bankAccount = data.bankAccountNumber ? data.bankAccountNumber.trim() : "";
    const hasBank = bankName.length > 0 && bankAccount.length > 0;

    merchantBankInfo = {
        hasBank: hasBank,
        bankName: hasBank ? bankName : "Vietcombank",
        bankAccountNumber: hasBank ? bankAccount : "999888777666",
        ownerName: data.merchantRestaurantName || "Cửa hàng"
    };

    const cardEl = document.getElementById('paymentMethodCARD');
    const cardRadio = document.getElementById('paymentRadioCARD');
    const notSupportedNotice = document.getElementById('bankNotSupportedNotice');
    const bankBox = document.getElementById('bankTransferInfoBox');

    if (cardEl) cardEl.classList.remove('disabled-payment-method');
    if (cardRadio) cardRadio.disabled = false;
    if (notSupportedNotice) notSupportedNotice.style.display = 'none';

    const bankNameEl = document.getElementById('checkoutMerchantBankName');
    if (bankNameEl) bankNameEl.innerText = merchantBankInfo.bankName;
    const bankAccEl = document.getElementById('checkoutMerchantBankAccount');
    if (bankAccEl) bankAccEl.innerText = merchantBankInfo.bankAccountNumber;
    const ownerEl = document.getElementById('checkoutMerchantOwnerName');
    if (ownerEl) ownerEl.innerText = merchantBankInfo.ownerName;

    // Nếu người dùng đang chọn CARD thì hiển thị box
    if (selectedPayment === 'CARD' && bankBox) {
        bankBox.style.display = 'block';
    }
}

// 4. TẢI VÀ RENDER GIỎ HÀNG TẠI CHECKOUT
async function loadCheckoutCart() {
    const cart = (typeof getGlobalCart === 'function') ? getGlobalCart() : [];
    const itemsList = document.getElementById('checkoutItemsList');
    const itemCount = document.getElementById('checkoutItemCount');
    const merchantTitle = document.getElementById('checkoutMerchantName');

    if (!itemsList) return;

    if (cart.length === 0) {
        itemsList.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="bi bi-cart-x fs-2 d-block text-secondary opacity-50 mb-2"></i>
                <p class="mb-2 fw-medium">Giỏ hàng của bạn đang trống!</p>
                <a href="/home" class="btn btn-outline-danger btn-sm rounded-pill px-3">Quay lại trang chủ</a>
            </div>
        `;
        if (itemCount) itemCount.innerText = '0 món';
        updatePriceSummary();
        return;
    }

    // Xác định merchantId ban đầu nếu đã có trong item giỏ hàng
    for (const it of cart) {
        if (it.merchantId) {
            currentMerchantId = it.merchantId;
            break;
        }
    }

    if (merchantTitle) {
        merchantTitle.innerText = cart[0].merchantName || 'Cửa hàng';
    }

    // Tải thông tin chính xác của Merchant và tài khoản ngân hàng từ món ăn đầu tiên
    try {
        const fetchFn = (typeof fetchWithAuth === 'function') ? fetchWithAuth : fetch;
        const res = await fetchFn(`/api/foods/${cart[0].id}/merchant-info`);
        if (res.ok) {
            const mData = await res.json();
            if (mData.merchantId) {
                currentMerchantId = mData.merchantId;
            }
            if (merchantTitle && mData.merchantRestaurantName) {
                merchantTitle.innerText = mData.merchantRestaurantName;
            }
            updateMerchantBankStatus(mData);
        }
    } catch (e) {
        console.warn('Không thể tải thông tin merchant từ API món ăn:', e);
    }

    // Dự phòng nếu chưa có merchantId
    if (!currentMerchantId && cart[0].merchantId) {
        currentMerchantId = cart[0].merchantId;
    }

    // Tải báo giá vận chuyển ngay khi biết merchantId và addressId
    if (currentSelectedAddress && currentSelectedAddress.id && currentMerchantId) {
        loadShippingQuotes(currentMerchantId, currentSelectedAddress.id);
    }

    // Render danh sách món ăn
    itemsList.innerHTML = '';
    subtotalPrice = 0;
    serviceFee = 0;
    let totalItems = 0;

    cart.forEach(item => {
        const price = item.discountPrice && item.discountPrice > 0 ? item.discountPrice : item.price;
        const itemSubtotal = price * item.quantity;
        subtotalPrice += itemSubtotal;
        totalItems += item.quantity;

        if (item.serviceFee && item.serviceFee > serviceFee) {
            serviceFee = item.serviceFee;
        }

        const div = document.createElement('div');
        div.className = 'd-flex justify-content-between align-items-center py-2.5 border-bottom border-light';
        div.innerHTML = `
            <div class="d-flex align-items-center gap-2.5 overflow-hidden">
                <img src="${item.image || '/images/default-food.png'}" 
                     class="food-item-img border" 
                     alt="${item.name}"
                     onerror="this.src='/images/default-food.png'">
                <div class="fw-bold text-dark fs-7">${item.quantity}x</div>
                <div class="overflow-hidden">
                    <div class="fw-semibold text-dark fs-7 text-truncate" title="${item.name}">${item.name}</div>
                    <small class="text-muted font-size-12">${formatCurrency(price)}</small>
                </div>
            </div>
            <div class="fw-bold text-dark font-size-14 text-nowrap">${formatCurrency(itemSubtotal)}</div>
        `;
        itemsList.appendChild(div);
    });

    if (itemCount) itemCount.innerText = `${totalItems} món`;
    updatePriceSummary();
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

document.addEventListener('DOMContentLoaded', () => {
    // 1. Khởi tạo hiển thị địa chỉ đang chọn từ danh sách modal
    const initialSelectedCard = document.querySelector('.address-modal-card.selected') || document.querySelector('.address-modal-card');
    if (initialSelectedCard) {
        const id = initialSelectedCard.getAttribute('data-id');
        const name = initialSelectedCard.getAttribute('data-name') || '';
        const phone = initialSelectedCard.getAttribute('data-phone') || '';
        const address = initialSelectedCard.getAttribute('data-address') || '';
        const note = initialSelectedCard.getAttribute('data-note') || '';
        const isDefault = initialSelectedCard.getAttribute('data-default') === 'true';
        currentSelectedAddress = { id, name, phone, address, note, isDefault };
    }

    // 2. Tải giỏ hàng
    loadCheckoutCart();

    // 3. ÁP DỤNG VOUCHER
    document.getElementById('btnApplyVoucher')?.addEventListener('click', () => {
        const codeInput = document.getElementById('voucherInput');
        const code = codeInput ? codeInput.value.trim().toUpperCase() : '';
        const msgEl = document.getElementById('voucherMessage');
        if (!code) {
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-danger';
                msgEl.innerText = 'Vui lòng nhập mã giảm giá';
            }
            return;
        }

        if (code === 'GIAM10K') {
            discountAmount = 10000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã GIAM10K: Giảm 10.000đ';
            }
        } else if (code === 'GIAM20K') {
            discountAmount = 20000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã GIAM20K: Giảm 20.000đ';
            }
        } else if (code === 'FREESHIP') {
            discountAmount = currentShippingFee;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã FREESHIP: Miễn phí vận chuyển';
            }
        } else {
            discountAmount = 0;
            appliedVoucher = '';
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-danger';
                msgEl.innerText = 'Mã giảm giá không hợp lệ hoặc đã hết hạn';
            }
        }

        updatePriceSummary();
    });

    // 4. LƯU ĐỊA CHỈ MỚI QUA API
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
            alert('Vui lòng điền đầy đủ các thông tin bắt buộc (*)');
            return;
        }

        const phoneRegex = /^(0|\+84)[0-9]{9,10}$/;
        if (!phoneRegex.test(contactPhone)) {
            alert('Số điện thoại không hợp lệ (ví dụ: 0987654321 hoặc +84987654321)');
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
                const modalEl = document.getElementById('newAddressModal');
                if (modalEl && typeof bootstrap !== 'undefined') {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }
                location.reload();
            } else {
                const err = await response.json();
                alert(err.message || 'Không thể lưu địa chỉ');
                if (saveBtn) {
                    saveBtn.disabled = false;
                    saveBtn.innerHTML = 'Lưu địa chỉ';
                }
            }
        } catch (e) {
            console.error('Lỗi lưu địa chỉ:', e);
            alert('Đã xảy ra lỗi kết nối');
            if (saveBtn) {
                saveBtn.disabled = false;
                saveBtn.innerHTML = 'Lưu địa chỉ';
            }
        }
    });

    // 5. XỬ LÝ ĐẶT HÀNG (PLACE ORDER)
    document.getElementById('btnPlaceOrder')?.addEventListener('click', async () => {
        if (!currentSelectedAddress || !currentSelectedAddress.address) {
            alert('Vui lòng chọn hoặc thêm địa chỉ giao hàng trước khi đặt món!');
            return;
        }

        const contactName = currentSelectedAddress.name;
        const contactPhone = currentSelectedAddress.phone;
        const deliveryAddress = currentSelectedAddress.address;
        const note = document.getElementById('orderNoteInput')?.value.trim() || '';

        const cart = (typeof getGlobalCart === 'function') ? getGlobalCart() : [];
        if (cart.length === 0) {
            alert('Giỏ hàng của bạn đang trống!');
            return;
        }

        const items = cart.map(i => ({
            foodId: i.id,
            quantity: i.quantity,
            note: i.note || ''
        }));

        const merchantIdToUse = currentMerchantId || (cart.length > 0 ? cart[0].merchantId : null);
        if (!merchantIdToUse) {
            alert('Không thể xác định thông tin Cửa hàng đối tác cho giỏ hàng này. Vui lòng tải lại trang và thử lại!');
            return;
        }

        const payload = {
            merchantId: merchantIdToUse,
            deliveryPartnerId: selectedDeliveryPartnerId,
            contactName: contactName,
            contactPhone: contactPhone,
            deliveryAddress: deliveryAddress,
            note: note,
            paymentMethod: selectedPayment,
            voucherCode: appliedVoucher,
            items: items
        };

        const btn = document.getElementById('btnPlaceOrder');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý đặt hàng...';
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

            const result = await response.json();
            if (result.success) {
                // Xóa giỏ hàng
                if (typeof saveGlobalCart === 'function') {
                    saveGlobalCart([]);
                } else {
                    localStorage.removeItem('mealchoice_cart');
                }

                // Chuyển hướng sang trang đặt hàng thành công
                window.location.href = `/checkout/success?orderCode=${result.orderCode}`;
            } else {
                alert(result.message || 'Đặt hàng thất bại, vui lòng thử lại');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-bag-check-fill me-2"></i>Đặt Hàng Ngay';
                }
            }
        } catch (e) {
            console.error('Lỗi đặt hàng:', e);
            alert('Đã xảy ra lỗi khi đặt hàng. Vui lòng kiểm tra lại kết nối!');
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-bag-check-fill me-2"></i>Đặt Hàng Ngay';
            }
        }
    });
});
