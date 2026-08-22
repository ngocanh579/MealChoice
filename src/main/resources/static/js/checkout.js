/**
 * Quản lý Trang Thanh toán Đơn hàng (Checkout)
 * File: static/js/checkout.js
 */

let selectedPayment = 'COD';
let appliedVoucher = '';
let discountAmount = 0;
const shippingFee = 15000;
let serviceFee = 0;
let subtotalPrice = 0;
let currentMerchantId = null;
let merchantBankInfo = null;

function formatCurrency(val) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(val)) + ' đ';
}

let currentSelectedAddress = null;

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
}

// 2. CHỌN PHƯƠNG THỨC THANH TOÁN
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
        if (method === 'CARD' && merchantBankInfo && merchantBankInfo.hasBank) {
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
        bankName: bankName,
        bankAccountNumber: bankAccount,
        ownerName: data.merchantRestaurantName || ""
    };

    const cardEl = document.getElementById('paymentMethodCARD');
    const cardRadio = document.getElementById('paymentRadioCARD');
    const notSupportedNotice = document.getElementById('bankNotSupportedNotice');
    const bankBox = document.getElementById('bankTransferInfoBox');

    if (!hasBank) {
        if (cardEl) {
            cardEl.classList.add('disabled-payment-method');
            cardEl.classList.remove('selected');
        }
        if (cardRadio) {
            cardRadio.disabled = true;
            cardRadio.checked = false;
        }
        if (notSupportedNotice) notSupportedNotice.style.display = 'block';
        if (bankBox) bankBox.style.display = 'none';

        // Tự động chuyển về COD nếu đang chọn CARD
        const codEl = document.getElementById('paymentMethodCOD');
        const codRadio = document.getElementById('paymentRadioCOD');
        if (codEl) codEl.classList.add('selected');
        if (codRadio) codRadio.checked = true;
        selectedPayment = 'COD';
    } else {
        if (cardEl) cardEl.classList.remove('disabled-payment-method');
        if (cardRadio) cardRadio.disabled = false;
        if (notSupportedNotice) notSupportedNotice.style.display = 'none';

        const bankNameEl = document.getElementById('checkoutMerchantBankName');
        if (bankNameEl) bankNameEl.innerText = bankName;
        const bankAccEl = document.getElementById('checkoutMerchantBankAccount');
        if (bankAccEl) bankAccEl.innerText = bankAccount;
        const ownerEl = document.getElementById('checkoutMerchantOwnerName');
        if (ownerEl) ownerEl.innerText = data.merchantRestaurantName || "Cửa hàng";

        if (selectedPayment === 'CARD' && bankBox) {
            bankBox.style.display = 'block';
        }
    }
}

// 3. TẢI VÀ RENDER GIỎ HÀNG TẠI CHECKOUT
async function loadCheckoutCart() {
    const cart = (typeof getGlobalCart === 'function') ? getGlobalCart() : [];
    const itemsList = document.getElementById('checkoutItemsList');
    const itemCount = document.getElementById('checkoutItemCount');
    const merchantTitle = document.getElementById('checkoutMerchantName');

    if (!itemsList) return;

    if (cart.length === 0) {
        itemsList.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="bi bi-basket fs-2 d-block opacity-50 mb-1"></i>
                <span>Chưa có món ăn nào trong giỏ hàng.</span>
                <div class="mt-2"><a href="/" class="btn btn-sm btn-danger rounded-pill px-3">Duyệt thực đơn</a></div>
            </div>
        `;
        document.getElementById('btnPlaceOrder')?.setAttribute('disabled', 'true');
        return;
    }

    // Lấy thông tin món đầu tiên để xác định Merchant ID & thông tin ngân hàng
    let firstItem = cart[0];
    try {
        const foodRes = await fetch(`/api/foods/${firstItem.id}/merchant-info`);
        if (foodRes.ok) {
            const foodData = await foodRes.json();
            currentMerchantId = foodData.merchantId;
            if (merchantTitle) {
                merchantTitle.innerText = foodData.merchantRestaurantName || "Cửa hàng MealChoice";
            }
            updateMerchantBankStatus(foodData);
        } else {
            currentMerchantId = "10000000-0000-0000-0000-000000000001";
            if (merchantTitle) merchantTitle.innerText = "Quán Ngon Hà Nội - Phở & Bún Chả";
            updateMerchantBankStatus({
                merchantRestaurantName: "Quán Ngon Hà Nội - Phở & Bún Chả",
                bankName: "Vietcombank",
                bankAccountNumber: "999888777666"
            });
        }
    } catch (e) {
        currentMerchantId = "10000000-0000-0000-0000-000000000001";
        if (merchantTitle) merchantTitle.innerText = "Quán Ngon Hà Nội - Phở & Bún Chả";
    }

    itemsList.innerHTML = '';
    subtotalPrice = 0;
    serviceFee = 0;
    let totalItems = 0;

    cart.forEach(item => {
        const price = item.discountPrice !== null && item.discountPrice !== undefined ? Number(item.discountPrice) : Number(item.price);
        const itemSubtotal = price * item.quantity;
        subtotalPrice += itemSubtotal;
        totalItems += item.quantity;
        if (item.serviceFee && Number(item.serviceFee) > serviceFee) {
            serviceFee = Number(item.serviceFee);
        }

        const div = document.createElement('div');
        div.className = 'd-flex align-items-center justify-content-between py-2 border-bottom';
        div.innerHTML = `
            <div class="d-flex align-items-center gap-2.5 pe-2 overflow-hidden">
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
    const total = subtotalPrice + shippingFee + serviceFee - discountAmount;
    const subEl = document.getElementById('summarySubtotal');
    if (subEl) subEl.innerText = formatCurrency(subtotalPrice);
    const shipEl = document.getElementById('summaryShippingFee');
    if (shipEl) shipEl.innerText = formatCurrency(shippingFee);
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
    loadCheckoutCart();

    // Khởi tạo hiển thị địa chỉ đang chọn từ danh sách modal
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

    // 4. ÁP DỤNG VOUCHER
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

        // Loại 1: Giảm theo số tiền cố định
        if (code === 'GIAM10K') {
            discountAmount = 10000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã GIAM10K: Giảm 10.000 đ vào đơn hàng!';
            }
        } else if (code === 'GIAM20K') {
            discountAmount = 20000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã GIAM20K: Giảm 20.000 đ vào đơn hàng!';
            }
        } else if (code === 'GIAM50K') {
            discountAmount = 50000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = 'Áp dụng mã GIAM50K: Giảm 50.000 đ vào đơn hàng!';
            }
        // Loại 2: Giảm theo phần trăm (%)
        } else if (code === 'GIAM10%' || code === 'GIAM10PT') {
            discountAmount = Math.round(subtotalPrice * 0.1);
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = `Áp dụng mã ${code}: Giảm 10% (-${formatCurrency(discountAmount)})!`;
            }
        } else if (code === 'GIAM20%' || code === 'GIAM20PT') {
            discountAmount = Math.round(subtotalPrice * 0.2);
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = `Áp dụng mã ${code}: Giảm 20% (-${formatCurrency(discountAmount)})!`;
            }
        } else if (code === 'GIAM50%' || code === 'GIAM50PT') {
            discountAmount = Math.round(subtotalPrice * 0.5);
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = `Áp dụng mã ${code}: Giảm 50% (-${formatCurrency(discountAmount)})!`;
            }
        } else {
            discountAmount = 10000;
            appliedVoucher = code;
            if (msgEl) {
                msgEl.className = 'font-size-12 mt-1 text-success';
                msgEl.innerText = `Áp dụng mã ${code}: Giảm 10.000 đ!`;
            }
        }

        updatePriceSummary();
    });

    // 5. LƯU ĐỊA CHỈ MỚI QUA API
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
                // Đóng modal và tải lại trang để nạp danh sách địa chỉ mới
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

    // 6. XỬ LÝ ĐẶT HÀNG (PLACE ORDER)
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

        const payload = {
            merchantId: currentMerchantId || "10000000-0000-0000-0000-000000000001",
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
