// API Base URL
const API_BASE = '/api/user';

// Global state
let currentUser = null;
let isEditing = false;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    checkAuthentication();
    setupEventListeners();
});

// Check if user is logged in
async function checkAuthentication() {
    try {
        const response = await fetch('/api/auth/check');
        const result = await response.json();

        if (!result.authenticated) {
            // Chưa đăng nhập -> redirect về login
            window.location.href = '/login';
            return;
        }

        // Đã đăng nhập -> load profile
        loadProfile();
    } catch (error) {
        console.error('Auth check error:', error);
        window.location.href = '/login';
    }
}

// Logout function
async function logout() {
    if (!confirm('Bạn có chắc muốn đăng xuất?')) {
        return;
    }

    try {
        await fetch('/api/auth/logout', { method: 'POST' });
        window.location.href = '/login';
    } catch (error) {
        console.error('Logout error:', error);
        alert('Lỗi khi đăng xuất');
    }
}

// Setup Event Listeners
function setupEventListeners() {
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileSubmit);
    }
    const addressForm = document.getElementById('addressForm');
    if (addressForm) {
        addressForm.addEventListener('submit', handleAddressSubmit);
    }
}

// ==================== PROFILE FUNCTIONS ====================

// Load user profile
async function loadProfile() {
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/profile`);
        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayProfile(currentUser);
            displayAddresses(currentUser.addresses);
        } else {
            showAlert('error', 'Không thể tải thông tin profile');
        }
    } catch (error) {
        console.error('Error loading profile:', error);
        showAlert('error', 'Lỗi kết nối server');
    } finally {
        showLoading(false);
    }
}

// Display profile data
function displayProfile(user) {
    document.getElementById('name').value = user.name || '';
    document.getElementById('email').value = user.email || '';
    document.getElementById('phoneNumber').value = user.phoneNumber || '';
    document.getElementById('dateOfBirth').value = user.dateOfBirth || '';
    document.getElementById('gender').value = user.gender || '';
}

// Enable profile editing
function editProfile() {
    isEditing = true;
    document.getElementById('name').disabled = false;
    document.getElementById('dateOfBirth').disabled = false;
    document.getElementById('gender').disabled = false;
    document.getElementById('profileActions').style.display = 'block';
}

// Cancel profile editing
function cancelEdit() {
    isEditing = false;
    document.getElementById('name').disabled = true;
    document.getElementById('dateOfBirth').disabled = true;
    document.getElementById('gender').disabled = true;
    document.getElementById('profileActions').style.display = 'none';
    displayProfile(currentUser); // Reset to original data
}

// Handle profile form submit
async function handleProfileSubmit(e) {
    e.preventDefault();

    const formData = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        gender: document.getElementById('gender').value || null,
        addresses: currentUser ? currentUser.addresses : [] // Keep existing addresses
    };

    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayProfile(currentUser);
            cancelEdit();
            showAlert('success', 'Cập nhật thông tin cá nhân thành công!');
        } else {
            showAlert('error', result.message || 'Cập nhật thất bại');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        showAlert('error', 'Lỗi kết nối server');
    } finally {
        showLoading(false);
    }
}

// ==================== ADDRESS FUNCTIONS ====================

// Display addresses
function displayAddresses(addresses) {
    const container = document.getElementById('addressList');
    if (!container) return;

    if (!addresses || addresses.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5 text-muted">
                <div class="display-6 mb-2">📭</div>
                <h6 class="fw-bold mb-1">Chưa có địa chỉ giao hàng nào</h6>
                <p class="small text-secondary mb-0">Hãy thêm địa chỉ để đặt hàng dễ dàng hơn!</p>
            </div>
        `;
        return;
    }

    container.innerHTML = addresses.map(addr => `
        <div class="col-md-6 col-12">
            <div class="card h-100 p-3 rounded-4 border ${addr.isDefault ? 'border-danger border-2 bg-light-subtle shadow-sm' : 'border-light-subtle shadow-sm'}">
                <div class="card-body p-2 d-flex flex-column justify-content-between">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h6 class="fw-bold text-dark mb-0">
                                <i class="bi bi-person-fill text-danger me-1"></i>${addr.contactName}
                            </h6>
                            ${addr.isDefault ? '<span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-2 py-1"><i class="bi bi-check-circle-fill me-1"></i>Mặc định</span>' : ''}
                        </div>
                        <p class="text-secondary small mb-2">
                            <i class="bi bi-telephone-fill me-1"></i><strong>${addr.contactPhone}</strong>
                        </p>
                        <p class="text-dark small mb-1">
                            <i class="bi bi-geo-alt-fill text-danger me-1"></i><strong>Địa chỉ:</strong> ${addr.street}
                        </p>
                        <p class="text-muted small mb-2 ms-4">
                            ${addr.ward}, ${addr.district}, ${addr.city}
                        </p>
                        ${addr.note ? `<p class="text-secondary small mb-2 bg-white p-2 rounded-3 border"><i class="bi bi-sticky me-1 text-warning"></i><strong>Ghi chú:</strong> ${addr.note}</p>` : ''}
                    </div>

                    <div class="d-flex justify-content-end align-items-center gap-2 pt-3 border-top mt-2">
                        ${!addr.isDefault ? `
                            <button class="btn btn-sm btn-outline-success rounded-pill px-3" onclick="setDefaultAddress(${addr.id})">
                                <i class="bi bi-star me-1"></i> Mặc định
                            </button>
                        ` : ''}
                        <button class="btn btn-sm btn-outline-primary rounded-pill px-3" onclick="editAddress(${addr.id})">
                            <i class="bi bi-pencil me-1"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger rounded-pill px-3" onclick="deleteAddress(${addr.id})">
                            <i class="bi bi-trash me-1"></i> Xóa
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// Open address modal
function openAddressModal(addressId = null) {
    const modalEl = document.getElementById('addressModal');
    if (!modalEl) return;
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const form = document.getElementById('addressForm');

    form.reset();
    document.getElementById('addressId').value = ''; // Reset hidden ID input

    const modalAlert = document.getElementById('modalAlertContainer');
    if (modalAlert) modalAlert.innerHTML = '';

    const contactNameInput = document.getElementById('contactName');
    const contactPhoneInput = document.getElementById('contactPhone');
    const contactNotice = document.getElementById('contactNotice');

    if (addressId) {
        const address = currentUser && currentUser.addresses ? currentUser.addresses.find(a => a.id === addressId) : null;
        if (address) {
            document.getElementById('modalTitle').innerHTML = '<i class="bi bi-pencil me-2 text-danger"></i>Chỉnh Sửa Địa Chỉ';
            document.getElementById('addressId').value = address.id;
            contactNameInput.value = address.contactName;
            contactPhoneInput.value = address.contactPhone;

            // QUY TẮC GEMINI.MD: Cho phép sửa địa chỉ, KHÔNG cho phép sửa liên hệ khi chỉnh sửa
            contactNameInput.disabled = true;
            contactPhoneInput.disabled = true;
            if (contactNotice) contactNotice.classList.remove('d-none');

            document.getElementById('city').value = address.city;
            document.getElementById('district').value = address.district;
            document.getElementById('ward').value = address.ward;
            document.getElementById('street').value = address.street;
            document.getElementById('note').value = address.note || '';
            document.getElementById('isDefault').checked = Boolean(address.isDefault);
        }
    } else {
        document.getElementById('modalTitle').innerHTML = '<i class="bi bi-house-add me-2 text-danger"></i>Thêm Địa Chỉ Mới';
        // Cho phép nhập liên hệ khi tạo địa chỉ mới
        contactNameInput.disabled = false;
        contactPhoneInput.disabled = false;
        if (contactNotice) contactNotice.classList.add('d-none');
    }

    modal.show();
}

// Close address modal
function closeAddressModal() {
    const modalEl = document.getElementById('addressModal');
    if (!modalEl) return;
    const modal = bootstrap.Modal.getInstance(modalEl);
    if (modal) {
        modal.hide();
    }
}

// Edit address
function editAddress(addressId) {
    openAddressModal(addressId);
}

// Handle address form submit (Xử lý cả Thêm và Sửa)
async function handleAddressSubmit(e) {
    e.preventDefault();

    const addressId = document.getElementById('addressId').value;

    const formData = {
        contactName: document.getElementById('contactName').value ? document.getElementById('contactName').value.trim() : '',
        contactPhone: document.getElementById('contactPhone').value ? document.getElementById('contactPhone').value.trim() : '',
        city: document.getElementById('city').value ? document.getElementById('city').value.trim() : '',
        district: document.getElementById('district').value ? document.getElementById('district').value.trim() : '',
        ward: document.getElementById('ward').value ? document.getElementById('ward').value.trim() : '',
        street: document.getElementById('street').value ? document.getElementById('street').value.trim() : '',
        note: document.getElementById('note').value ? document.getElementById('note').value.trim() : null,
        isDefault: document.getElementById('isDefault').checked
    };

    // Xác định URL và HTTP Method dựa vào việc có addressId hay không
    const isEdit = Boolean(addressId);
    const url = isEdit ? `${API_BASE}/address/${addressId}` : `${API_BASE}/address`;
    const method = isEdit ? 'PUT' : 'POST';

    showLoading(true);
    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayAddresses(currentUser.addresses);
            closeAddressModal();
            showAlert('success', isEdit ? 'Cập nhật địa chỉ thành công!' : 'Lưu địa chỉ mới thành công!');
        } else {
            showAlert('error', result.message || 'Lưu địa chỉ thất bại', 'modalAlertContainer');
        }
    } catch (error) {
        console.error('Error saving address:', error);
        showAlert('error', 'Lỗi kết nối server', 'modalAlertContainer');
    } finally {
        showLoading(false);
    }
}

// Delete address
async function deleteAddress(addressId) {
    if (!confirm('⚠️ Bạn có chắc muốn xóa địa chỉ này?')) {
        return;
    }

    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/address/${addressId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayAddresses(currentUser.addresses);
            showAlert('success', 'Xóa địa chỉ thành công!');
        } else {
            showAlert('error', result.message || 'Xóa địa chỉ thất bại');
        }
    } catch (error) {
        console.error('Error deleting address:', error);
        showAlert('error', 'Lỗi kết nối server');
    } finally {
        showLoading(false);
    }
}

// Set default address
async function setDefaultAddress(addressId) {
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/address/${addressId}/set-default`, {
            method: 'PATCH'
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayAddresses(currentUser.addresses);
            showAlert('success', 'Đã đặt địa chỉ mặc định!');
        } else {
            showAlert('error', result.message || 'Thất bại');
        }
    } catch (error) {
        console.error('Error setting default address:', error);
        showAlert('error', 'Lỗi kết nối server');
    } finally {
        showLoading(false);
    }
}

// ==================== UTILITY FUNCTIONS ====================

// Show alert
function showAlert(type, message, containerId = 'alertContainer') {
    const container = document.getElementById(containerId) || document.getElementById('alertContainer');
    if (!container) return;
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';

    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show rounded-3 shadow-sm d-flex align-items-center mb-3" role="alert">
            <i class="bi ${icon} fs-5 me-2"></i>
            <div>${message}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    setTimeout(() => {
        container.innerHTML = '';
    }, 6000);
}

// Show/hide loading
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (loading) {
        if (show) {
            loading.classList.remove('d-none');
            loading.classList.add('d-flex');
        } else {
            loading.classList.remove('d-flex');
            loading.classList.add('d-none');
        }
    }
}