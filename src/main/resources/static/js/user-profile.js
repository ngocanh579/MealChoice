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
    document.getElementById('profileForm').addEventListener('submit', handleProfileSubmit);
    document.getElementById('addressForm').addEventListener('submit', handleAddressSubmit);
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
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        gender: document.getElementById('gender').value || null,
        addresses: currentUser.addresses // Keep existing addresses
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
            showAlert('success', '✅ Cập nhật thông tin thành công!');
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

    if (!addresses || addresses.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #999;">
                <p style="font-size: 48px;">📭</p>
                <p>Chưa có địa chỉ giao hàng nào</p>
                <p style="font-size: 14px; margin-top: 10px;">Hãy thêm địa chỉ để đặt hàng dễ dàng hơn!</p>
            </div>
        `;
        return;
    }

    container.innerHTML = addresses.map(addr => `
        <div class="address-item ${addr.isDefault ? 'default' : ''}">
            ${addr.isDefault ? '<span class="default-badge">⭐ Mặc định</span>' : ''}
            
            <div class="address-info">
                <h4>👤 ${addr.contactName} - 📞 ${addr.contactPhone}</h4>
                <p><strong>📍 Địa chỉ:</strong> ${addr.street}</p>
                <p>${addr.ward}, ${addr.district}, ${addr.city}</p>
                ${addr.note ? `<p><strong>📝 Ghi chú:</strong> ${addr.note}</p>` : ''}
            </div>

            <div class="address-actions">
                ${!addr.isDefault ? `
                    <button class="btn btn-sm btn-success" onclick="setDefaultAddress(${addr.id})">
                        ⭐ Đặt làm mặc định
                    </button>
                ` : ''}
                <button class="btn btn-sm btn-primary" onclick="editAddress(${addr.id})">
                    ✏️ Sửa
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteAddress(${addr.id})">
                    🗑️ Xóa
                </button>
            </div>
        </div>
    `).join('');
}

// Open address modal
function openAddressModal(addressId = null) {
    const modal = document.getElementById('addressModal');
    const form = document.getElementById('addressForm');

    form.reset();
    document.getElementById('addressId').value = ''; // Reset hidden ID input

    if (addressId) {
        const address = currentUser.addresses.find(a => a.id === addressId);
        if (address) {
            document.getElementById('modalTitle').textContent = '✏️ Chỉnh Sửa Địa Chỉ';
            document.getElementById('addressId').value = address.id;
            document.getElementById('contactName').value = address.contactName;
            document.getElementById('contactPhone').value = address.contactPhone;
            document.getElementById('city').value = address.city;
            document.getElementById('district').value = address.district;
            document.getElementById('ward').value = address.ward;
            document.getElementById('street').value = address.street;
            document.getElementById('note').value = address.note || '';
            document.getElementById('isDefault').checked = address.isDefault;
        }
    } else {
        document.getElementById('modalTitle').textContent = '➕ Thêm Địa Chỉ Mới';
    }

    modal.classList.add('active');
}

// Close address modal
function closeAddressModal() {
    document.getElementById('addressModal').classList.remove('active');
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
        contactName: document.getElementById('contactName').value,
        contactPhone: document.getElementById('contactPhone').value,
        city: document.getElementById('city').value,
        district: document.getElementById('district').value,
        ward: document.getElementById('ward').value,
        street: document.getElementById('street').value,
        note: document.getElementById('note').value || null,
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
            showAlert('success', isEdit ? '✅ Cập nhật địa chỉ thành công!' : '✅ Lưu địa chỉ mới thành công!');
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
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            currentUser = result.data;
            displayAddresses(currentUser.addresses);
            showAlert('success', '✅ Xóa địa chỉ thành công!');
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
            showAlert('success', '✅ Đã đặt địa chỉ mặc định!');
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
    const alertClass = type === 'success' ? 'alert-success' : 'alert-error';

    container.innerHTML = `
        <div class="alert ${alertClass}">
            ${type === 'success' ? '✅' : '❌'} ${message}
        </div>
    `;

    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

// Show/hide loading
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (show) {
        loading.classList.add('active');
    } else {
        loading.classList.remove('active');
    }
}