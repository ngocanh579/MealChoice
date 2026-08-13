// API Base URL
const API_BASE = '/api/user';

// Global state
let currentUser = null;
let isEditing = false;
let initialProfileData = {};
let initialAddressData = {};

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    checkAuthentication();
    setupEventListeners();
    // Tự động làm mới access token trước khi hết hạn
    silentRefresh();
    setInterval(silentRefresh, 14 * 60 * 1000); // Mỗi 14 phút
});

// Helper to get Auth Headers with JWT Bearer Token
function getAuthHeaders(extraHeaders = {}) {
    const token = localStorage.getItem('accessToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...extraHeaders
    };
}

// Check if user is logged in
function checkAuthentication() {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/login';
        return;
    }
    loadProfile();
}

async function silentRefresh() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return;

    try {
        const response = await fetch('/api/auth/refresh-token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (response.ok) {
            const data = await response.json();
            if (data.accessToken) {
                localStorage.setItem('accessToken', data.accessToken);
                document.cookie = `accessToken=${data.accessToken}; path=/; max-age=86400; SameSite=Lax`;
            }
        } else if (response.status === 401 || response.status === 403) {
            // Refresh token hết hạn → logout
            console.warn('Refresh token hết hạn, đăng xuất...');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
        }
    } catch (e) {
        console.warn('Silent refresh tạm thời thất bại:', e);
    }
}

// Logout function
async function logout() {
    if (!confirm('Bạn có chắc muốn đăng xuất?')) {
        return;
    }

    try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ refreshToken })
            });
        }
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
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
        const response = await fetch(`${API_BASE}/profile`, {
            headers: getAuthHeaders()
        });

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
            return;
        }

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayProfile(currentUser);
            displayAddresses(currentUser.addresses);
        } else {
            showAlert('error', result.message || 'Không thể tải thông tin profile');
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
    const displayNameEl = document.getElementById('displayName');
    if (displayNameEl) displayNameEl.value = user.displayName || user.name || '';

    document.getElementById('email').value = user.email || '';
    document.getElementById('phoneNumber').value = user.phoneNumber || '';

    const dobEl = document.getElementById('dob');
    if (dobEl) {
        let dobValue = user.dob || user.dateOfBirth || '';
        if (dobValue && dobValue.includes('T')) {
            dobValue = dobValue.split('T')[0];
        }
        dobEl.value = dobValue;
    }

    document.getElementById('gender').value = user.gender || '';
}

// Enable profile editing
function editProfile() {
    isEditing = true;
    const displayNameEl = document.getElementById('displayName');
    if (displayNameEl) displayNameEl.disabled = false;
    const dobEl = document.getElementById('dob');
    if (dobEl) dobEl.disabled = false;
    document.getElementById('gender').disabled = false;
    document.getElementById('profileActions').style.display = 'block';

    let dobValue = currentUser.dob || currentUser.dateOfBirth || '';
    if (dobValue && dobValue.includes('T')) {
        dobValue = dobValue.split('T')[0];
    }

    // Lưu dữ liệu ban đầu để so sánh dirty fields
    initialProfileData = {
        displayName: displayNameEl ? displayNameEl.value : '',
        dob: dobValue,
        gender: document.getElementById('gender').value || ''
    };
}

// Cancel profile editing
function cancelEdit() {
    isEditing = false;
    const displayNameEl = document.getElementById('displayName');
    if (displayNameEl) displayNameEl.disabled = true;
    const dobEl = document.getElementById('dob');
    if (dobEl) dobEl.disabled = true;
    document.getElementById('gender').disabled = true;
    document.getElementById('profileActions').style.display = 'none';
    displayProfile(currentUser); // Reset to original data
}

// Handle profile form submit (Partial Update via PATCH)
async function handleProfileSubmit(e) {
    e.preventDefault();

    const currentDisplayName = document.getElementById('displayName').value;
    const currentDob = document.getElementById('dob').value;
    const currentGender = document.getElementById('gender').value || '';

    // Chỉ đưa vào payload các trường có sự thay đổi (dirty fields)
    const payload = {};
    if (currentDisplayName !== initialProfileData.displayName) {
        payload.displayName = currentDisplayName;
    }
    if (currentDob !== initialProfileData.dob) {
        payload.dob = currentDob ? currentDob + 'T00:00:00' : null;
    }
    if (currentGender !== initialProfileData.gender) {
        payload.gender = currentGender || null;
    }

    if (Object.keys(payload).length === 0) {
        showAlert('info', 'Không có thông tin nào thay đổi');
        cancelEdit();
        return;
    }

    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/profile`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
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

            // Lưu dữ liệu ban đầu để so sánh khi sửa địa chỉ
            initialAddressData = {
                city: address.city || '',
                district: address.district || '',
                ward: address.ward || '',
                street: address.street || '',
                note: address.note || '',
                isDefault: Boolean(address.isDefault)
            };
        }
    } else {
        document.getElementById('modalTitle').innerHTML = '<i class="bi bi-house-add me-2 text-danger"></i>Thêm Địa Chỉ Mới';
        // Cho phép nhập liên hệ khi tạo địa chỉ mới
        contactNameInput.disabled = false;
        contactPhoneInput.disabled = false;
        if (contactNotice) contactNotice.classList.add('d-none');
        initialAddressData = {};
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

// Handle address form submit (Thêm: POST đầy đủ, Sửa: PATCH partial update)
async function handleAddressSubmit(e) {
    e.preventDefault();

    const addressId = document.getElementById('addressId').value;
    const isEdit = Boolean(addressId);

    let formData;
    let url;
    let method;

    if (isEdit) {
        url = `${API_BASE}/address/${addressId}`;
        method = 'PATCH';

        const currentCity = document.getElementById('city').value;
        const currentDistrict = document.getElementById('district').value;
        const currentWard = document.getElementById('ward').value;
        const currentStreet = document.getElementById('street').value;
        const currentNote = document.getElementById('note').value || '';
        const currentIsDefault = document.getElementById('isDefault').checked;

        formData = {};
        if (currentCity !== initialAddressData.city) formData.city = currentCity;
        if (currentDistrict !== initialAddressData.district) formData.district = currentDistrict;
        if (currentWard !== initialAddressData.ward) formData.ward = currentWard;
        if (currentStreet !== initialAddressData.street) formData.street = currentStreet;
        if (currentNote !== initialAddressData.note) formData.note = currentNote;
        if (currentIsDefault !== initialAddressData.isDefault) formData.isDefault = currentIsDefault;

        if (Object.keys(formData).length === 0) {
            showAlert('info', 'Không có thông tin địa chỉ nào thay đổi');
            closeAddressModal();
            return;
        }
    } else {
        url = `${API_BASE}/address`;
        method = 'POST';
        formData = {
            contactName: document.getElementById('contactName').value,
            contactPhone: document.getElementById('contactPhone').value,
            city: document.getElementById('city').value,
            district: document.getElementById('district').value,
            ward: document.getElementById('ward').value,
            street: document.getElementById('street').value,
            note: document.getElementById('note').value || null,
            isDefault: document.getElementById('isDefault').checked
        };
    }

    showLoading(true);
    try {
        const response = await fetch(url, {
            method: method,
            headers: getAuthHeaders(),
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayAddresses(currentUser.addresses);
            closeAddressModal();
            showAlert('success', isEdit ? 'Cập nhật địa chỉ thành công!' : 'Lưu địa chỉ mới thành công!');
        } else {
            showAlert('error', result.message || 'Lưu địa chỉ thất bại');
        }
    } catch (error) {
        console.error('Error saving address:', error);
        showAlert('error', 'Lỗi kết nối server');
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
            method: 'DELETE',
            headers: getAuthHeaders()
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
            method: 'PATCH',
            headers: getAuthHeaders()
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
function showAlert(type, message) {
    const container = document.getElementById('alertContainer');
    if (!container) return;
    let alertClass = 'alert-danger';
    let icon = 'bi-exclamation-triangle-fill';

    if (type === 'success') {
        alertClass = 'alert-success';
        icon = 'bi-check-circle-fill';
    } else if (type === 'info') {
        alertClass = 'alert-info';
        icon = 'bi-info-circle-fill';
    }

    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show rounded-3 shadow-sm d-flex align-items-center mb-4" role="alert">
            <i class="bi ${icon} fs-5 me-2"></i>
            <div>${message}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
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