// API Base URL
const API_BASE = '/api/user';
const PROVINCES_API = 'https://provinces.open-api.vn/api/';

// Global state
let currentUser = null;
let isEditing = false;
let initialProfileData = {};
let initialAddressData = {};
let cachedProvinces = null;

// Tải danh sách tỉnh thành (có cache)
async function getProvinces() {
    if (cachedProvinces) {
        return cachedProvinces;
    }

    if (window.provincesLoadedPromise) {
        return window.provincesLoadedPromise;
    }

    window.provincesLoadedPromise = fetch(`${PROVINCES_API}?depth=1`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải danh sách tỉnh/thành phố');
            }
            return response.json();
        })
        .then(data => {
            cachedProvinces = data;
            return data;
        })
        .catch(error => {
            console.error('Lỗi tải tỉnh/thành phố:', error);
            window.provincesLoadedPromise = null;
            return [];
        });

    return window.provincesLoadedPromise;
}

// Tải quận/huyện theo tỉnh/thành phố
async function getDistrictsByProvinceCode(provinceCode) {
    if (!provinceCode) {
        return [];
    }

    try {
        const response = await fetch(`${PROVINCES_API}p/${provinceCode}?depth=2`);
        if (!response.ok) {
            throw new Error('Không thể tải danh sách quận/huyện');
        }

        const data = await response.json();
        return data.districts || [];
    } catch (error) {
        console.error('Lỗi tải quận/huyện:', error);
        return [];
    }
}

// Tải phường/xã theo quận/huyện
async function getWardsByDistrictCode(distCode) {
    if (!distCode) {
        return [];
    }

    try {
        const response = await fetch(`${PROVINCES_API}d/${distCode}?depth=2`);
        if (!response.ok) {
            throw new Error('Không thể tải danh sách phường/xã');
        }

        const data = await response.json();
        return data.wards || [];
    } catch (error) {
        console.error('Lỗi tải phường/xã:', error);
        return [];
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    checkAuthentication();
    setupEventListeners();
    setupAddressSelectListeners();
    getProvinces();
});

// Helper lấy Auth Headers với JWT Bearer Token
function getAuthHeaders() {
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token');

    return {
        'Content-Type': 'application/json',
        ...(token ? {Authorization: `Bearer ${token}`} : {})
    };
}

// Kiểm tra authentication
async function checkAuthentication() {
    try {
        const response = await fetch(`${API_BASE}/profile`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                window.location.href = '/login';
                return;
            }

            throw new Error('Không thể lấy thông tin người dùng');
        }

        currentUser = await response.json();
        displayProfile(currentUser);
        loadAddresses();
    } catch (error) {
        console.error('Lỗi kiểm tra đăng nhập:', error);
        showAlert('danger', 'Không thể tải thông tin tài khoản');
    }
}

// Hiển thị thông tin cá nhân
function displayProfile(user) {
    const displayName = document.getElementById('displayName');
    const email = document.getElementById('email');
    const phoneNumber = document.getElementById('phoneNumber');
    const gender = document.getElementById('gender');

    if (displayName) {
        displayName.value = user.displayName || '';
    }

    if (email) {
        email.value = user.email || '';
    }

    if (phoneNumber) {
        phoneNumber.value = user.phoneNumber || '';
    }

    if (gender) {
        gender.value = user.gender || '';
    }

    initialProfileData = {
        displayName: user.displayName || '',
        phoneNumber: user.phoneNumber || '',
        gender: user.gender || ''
    };

    const profileName = document.getElementById('profileName');
    if (profileName) {
        profileName.textContent = user.displayName || 'Người dùng';
    }

    const profileEmail = document.getElementById('profileEmail');
    if (profileEmail) {
        profileEmail.textContent = user.email || '';
    }
}

// Cài đặt event listener
function setupEventListeners() {
    const editProfileBtn = document.getElementById('btnEditProfile');
    const cancelProfileBtn = document.getElementById('btnCancelProfile');
    const profileForm = document.getElementById('profileForm');

    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', enableProfileEditing);
    }

    if (cancelProfileBtn) {
        cancelProfileBtn.addEventListener('click', disableProfileEditing);
    }

    if (profileForm) {
        profileForm.addEventListener('submit', submitProfileForm);
    }

    const addAddressBtn = document.getElementById('btnAddAddress');

    if (addAddressBtn) {
        addAddressBtn.addEventListener('click', () => openAddressModal());
    }

    const addressForm = document.getElementById('addressForm');

    if (addressForm) {
        addressForm.addEventListener('submit', event => {
            event.preventDefault();
            submitAddressForm();
        });
    }

    const closeAddressBtn = document.getElementById('btnCloseAddressModal');

    if (closeAddressBtn) {
        closeAddressBtn.addEventListener('click', closeAddressModal);
    }

    const cancelAddressBtn = document.getElementById('btnCancelAddress');

    if (cancelAddressBtn) {
        cancelAddressBtn.addEventListener('click', closeAddressModal);
    }

    const deleteAddressBtn = document.getElementById('btnDeleteAddress');

    if (deleteAddressBtn) {
        deleteAddressBtn.addEventListener('click', deleteCurrentAddress);
    }
}

// Bật chế độ chỉnh sửa thông tin cá nhân
function enableProfileEditing() {
    isEditing = true;

    ['displayName', 'phoneNumber', 'gender'].forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.disabled = false;
        }
    });

    const editProfileBtn = document.getElementById('btnEditProfile');
    const saveProfileBtn = document.getElementById('btnSaveProfile');
    const cancelProfileBtn = document.getElementById('btnCancelProfile');

    if (editProfileBtn) {
        editProfileBtn.classList.add('d-none');
    }

    if (saveProfileBtn) {
        saveProfileBtn.classList.remove('d-none');
    }

    if (cancelProfileBtn) {
        cancelProfileBtn.classList.remove('d-none');
    }
}

// Tắt chế độ chỉnh sửa thông tin cá nhân
function disableProfileEditing() {
    isEditing = false;

    const displayName = document.getElementById('displayName');
    const phoneNumber = document.getElementById('phoneNumber');
    const gender = document.getElementById('gender');

    if (displayName) {
        displayName.value = initialProfileData.displayName;
        displayName.disabled = true;
    }

    if (phoneNumber) {
        phoneNumber.value = initialProfileData.phoneNumber;
        phoneNumber.disabled = true;
    }

    if (gender) {
        gender.value = initialProfileData.gender;
        gender.disabled = true;
    }

    const editProfileBtn = document.getElementById('btnEditProfile');
    const saveProfileBtn = document.getElementById('btnSaveProfile');
    const cancelProfileBtn = document.getElementById('btnCancelProfile');

    if (editProfileBtn) {
        editProfileBtn.classList.remove('d-none');
    }

    if (saveProfileBtn) {
        saveProfileBtn.classList.add('d-none');
    }

    if (cancelProfileBtn) {
        cancelProfileBtn.classList.add('d-none');
    }
}

// Submit thông tin cá nhân
async function submitProfileForm(event) {
    event.preventDefault();

    const displayName = document.getElementById('displayName')?.value.trim() || '';
    const phoneNumber = document.getElementById('phoneNumber')?.value.trim() || '';
    const gender = document.getElementById('gender')?.value || '';

    const formData = {
        displayName,
        phoneNumber,
        gender
    };

    try {
        const response = await fetch(`${API_BASE}/profile`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'Không thể cập nhật thông tin');
        }

        currentUser = result;
        displayProfile(currentUser);
        disableProfileEditing();

        showAlert('success', 'Cập nhật thông tin cá nhân thành công');
    } catch (error) {
        console.error('Lỗi cập nhật thông tin cá nhân:', error);
        showAlert('danger', error.message || 'Có lỗi xảy ra khi cập nhật thông tin');
    }
}

// Tải danh sách địa chỉ
async function loadAddresses() {
    try {
        const response = await fetch(`${API_BASE}/addresses`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error('Không thể tải danh sách địa chỉ');
        }

        const addresses = await response.json();
        displayAddresses(addresses);
    } catch (error) {
        console.error('Lỗi tải địa chỉ:', error);
        showAlert('danger', 'Không thể tải danh sách địa chỉ');
    }
}

// Hiển thị danh sách địa chỉ
function displayAddresses(addresses) {
    const container = document.getElementById('addressList');

    if (!container) {
        return;
    }

    if (!addresses || addresses.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="bi bi-geo-alt fs-1 d-block mb-2 opacity-50"></i>
                <p class="mb-0">Bạn chưa có địa chỉ nhận hàng nào.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = addresses.map(address => `
        <div class="address-card ${address.isDefault ? 'selected' : ''}">
            <div class="d-flex justify-content-between align-items-start gap-3">
                <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <strong class="text-dark">${escapeHtml(address.contactName || '')}</strong>
                        <span class="text-muted">|</span>
                        <span class="text-danger fw-semibold">${escapeHtml(address.contactPhone || '')}</span>
                        ${address.isDefault ? '<span class="badge bg-danger rounded-pill">Mặc định</span>' : ''}
                    </div>
                    <div class="text-secondary small">
                        ${escapeHtml(formatAddress(address))}
                    </div>
                    ${address.note ? `
                        <div class="text-muted font-size-12 fst-italic mt-1">
                            <i class="bi bi-info-circle me-1"></i>
                            ${escapeHtml(address.note)}
                        </div>
                    ` : ''}
                </div>
                <div class="d-flex gap-2">
                    <button type="button"
                            class="btn btn-sm btn-outline-danger rounded-pill"
                            onclick="openAddressModal('${address.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button type="button"
                            class="btn btn-sm btn-outline-secondary rounded-pill"
                            onclick="deleteAddress('${address.id}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// Gắn listener thay đổi dropdown địa chỉ
function setupAddressSelectListeners() {
    const citySelect = document.getElementById('city');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');

    if (!citySelect || !districtSelect || !wardSelect) {
        return;
    }

    citySelect.addEventListener('change', async () => {
        districtSelect.innerHTML = '<option value="" selected disabled>Đang tải Quận/Huyện...</option>';
        wardSelect.innerHTML = '<option value="" selected disabled>Phường/Xã</option>';

        districtSelect.disabled = true;
        wardSelect.disabled = true;

        const provinceCode = citySelect.value;

        if (!provinceCode) {
            districtSelect.innerHTML = '<option value="" selected disabled>Quận/Huyện</option>';
            return;
        }

        await loadDistrictsByCity(citySelect);
    });

    districtSelect.addEventListener('change', async () => {
        wardSelect.innerHTML = '<option value="" selected disabled>Đang tải Phường/Xã...</option>';
        wardSelect.disabled = true;

        const districtCode = districtSelect.value;

        if (!districtCode) {
            wardSelect.innerHTML = '<option value="" selected disabled>Phường/Xã</option>';
            return;
        }

        await loadWardsByDistrict(districtSelect);
    });
}

// Tải danh sách tỉnh vào select
async function loadProvincesIntoSelect(selectedCity = '') {
    const citySelect = document.getElementById('city');

    if (!citySelect) {
        return;
    }

    citySelect.innerHTML = '<option value="" selected disabled>Đang tải Tỉnh/Thành phố...</option>';

    const provinces = await getProvinces();

    if (!provinces.length) {
        citySelect.innerHTML = '<option value="" selected disabled>Không thể tải dữ liệu</option>';
        return;
    }

    citySelect.innerHTML = `
        <option value="" disabled ${!selectedCity ? 'selected' : ''}>Tỉnh/Thành phố</option>
        ${provinces.map(province => `
            <option value="${province.code}">${escapeHtml(province.name)}</option>
        `).join('')}
    `;

    if (selectedCity) {
        const selectedOption = Array.from(citySelect.options)
            .find(option => option.textContent.trim() === selectedCity.trim());

        if (selectedOption) {
            citySelect.value = selectedOption.value;
        }
    }
}

// Tải quận/huyện theo tỉnh
async function loadDistrictsByCity(citySelect) {
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');

    if (!districtSelect || !wardSelect || !citySelect.value) {
        return;
    }

    districtSelect.innerHTML = '<option value="" selected disabled>Đang tải Quận/Huyện...</option>';
    districtSelect.disabled = true;

    wardSelect.innerHTML = '<option value="" selected disabled>Phường/Xã</option>';
    wardSelect.disabled = true;

    const districts = await getDistrictsByProvinceCode(citySelect.value);

    districtSelect.innerHTML = `
        <option value="" selected disabled>Quận/Huyện</option>
        ${districts.map(district => `
            <option value="${district.code}">${escapeHtml(district.name)}</option>
        `).join('')}
    `;

    districtSelect.disabled = false;
}

// Tải phường/xã theo quận
async function loadWardsByDistrict(districtSelect) {
    const wardSelect = document.getElementById('ward');

    if (!wardSelect || !districtSelect.value) {
        return;
    }

    wardSelect.innerHTML = '<option value="" selected disabled>Đang tải Phường/Xã...</option>';
    wardSelect.disabled = true;

    const wards = await getWardsByDistrictCode(districtSelect.value);

    wardSelect.innerHTML = `
        <option value="" selected disabled>Phường/Xã</option>
        ${wards.map(ward => `
            <option value="${ward.code}">${escapeHtml(ward.name)}</option>
        `).join('')}
    `;

    wardSelect.disabled = false;
}

// Open address modal (Tỉnh/Huyện/Xã)
async function openAddressModal(addressId = null) {
    const modalEl = document.getElementById('addressModal');

    if (!modalEl) {
        return;
    }

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const citySelect = document.getElementById('city');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const contactNameInput = document.getElementById('contactName');
    const contactPhoneInput = document.getElementById('contactPhone');

    if (!citySelect || !districtSelect || !wardSelect) {
        return;
    }

    isEditing = Boolean(addressId);

    if (addressId) {
        try {
            const response = await fetch(`${API_BASE}/address/${addressId}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error('Không thể lấy thông tin địa chỉ');
            }

            const address = await response.json();

            document.getElementById('modalTitle').innerHTML =
                '<i class="bi bi-pencil me-2 text-danger"></i>Chỉnh Sửa Địa Chỉ';

            document.getElementById('addressId').value = address.id;

            contactNameInput.value = address.contactName || '';
            contactPhoneInput.value = address.contactPhone || '';

            contactNameInput.disabled = false;
            contactPhoneInput.disabled = true;

            document.getElementById('street').value = address.street || '';
            document.getElementById('note').value = address.note || '';
            document.getElementById('isDefault').checked = Boolean(address.isDefault);

            const oldCity = (address.city || '').trim();
            const oldDistrict = (address.district || '').trim();
            const oldWard = (address.ward || '').trim();

            citySelect.innerHTML =
                '<option value="" selected disabled>Đang tải Tỉnh/Thành phố...</option>';

            districtSelect.innerHTML =
                '<option value="" selected disabled>Quận/Huyện</option>';

            wardSelect.innerHTML =
                '<option value="" selected disabled>Phường/Xã</option>';

            districtSelect.disabled = true;
            wardSelect.disabled = true;

            initialAddressData = {
                city: oldCity,
                district: oldDistrict,
                ward: oldWard,
                street: address.street || '',
                note: address.note || '',
                isDefault: Boolean(address.isDefault)
            };

            modal.show();

            const provinces = await getProvinces();

            citySelect.innerHTML = `
                <option value="" disabled ${!oldCity ? 'selected' : ''}>Tỉnh/Thành phố</option>
                ${provinces.map(province => `
                    <option value="${province.code}">${escapeHtml(province.name)}</option>
                `).join('')}
            `;

            const selectedCity = provinces.find(
                province => province.name.trim() === oldCity
            );

            if (selectedCity) {
                citySelect.value = selectedCity.code;

                await loadDistrictsByCity(citySelect);

                const selectedDistrict = Array.from(districtSelect.options).find(
                    option => option.textContent.trim() === oldDistrict
                );

                if (selectedDistrict) {
                    districtSelect.value = selectedDistrict.value;

                    await loadWardsByDistrict(districtSelect);

                    const selectedWard = Array.from(wardSelect.options).find(
                        option => option.textContent.trim() === oldWard
                    );

                    if (selectedWard) {
                        wardSelect.value = selectedWard.value;
                    }
                }
            }
        } catch (error) {
            console.error('Lỗi nạp địa chỉ cũ:', error);
            showAlert('danger', 'Không thể tải thông tin địa chỉ');
        }

        return;
    }

    document.getElementById('modalTitle').innerHTML =
        '<i class="bi bi-plus-circle me-2 text-danger"></i>Thêm Địa Chỉ';

    document.getElementById('addressId').value = '';

    contactNameInput.value = '';
    contactPhoneInput.value = '';
    contactNameInput.disabled = false;
    contactPhoneInput.disabled = false;

    document.getElementById('street').value = '';
    document.getElementById('note').value = '';
    document.getElementById('isDefault').checked = false;

    citySelect.innerHTML =
        '<option value="" selected disabled>Đang tải Tỉnh/Thành phố...</option>';

    districtSelect.innerHTML =
        '<option value="" selected disabled>Quận/Huyện</option>';

    wardSelect.innerHTML =
        '<option value="" selected disabled>Phường/Xã</option>';

    districtSelect.disabled = true;
    wardSelect.disabled = true;

    initialAddressData = {};

    modal.show();

    const provinces = await getProvinces();

    citySelect.innerHTML = `
        <option value="" selected disabled>Tỉnh/Thành phố</option>
        ${provinces.map(province => `
            <option value="${province.code}">${escapeHtml(province.name)}</option>
        `).join('')}
    `;
}

// Close address modal
function closeAddressModal() {
    const modalEl = document.getElementById('addressModal');

    if (!modalEl) {
        return;
    }

    const modal = bootstrap.Modal.getInstance(modalEl);

    if (modal) {
        modal.hide();
    }

    isEditing = false;
    initialAddressData = {};
}

// Submit address form
async function submitAddressForm() {
    const addressId = document.getElementById('addressId').value;
    const isEdit = Boolean(addressId);

    const contactName = document.getElementById('contactName').value.trim();
    const contactPhone = document.getElementById('contactPhone').value.trim();
    const citySelect = document.getElementById('city');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');

    const city = citySelect.options[citySelect.selectedIndex]?.textContent.trim() || '';
    const district = districtSelect.options[districtSelect.selectedIndex]?.textContent.trim() || '';
    const ward = wardSelect.options[wardSelect.selectedIndex]?.textContent.trim() || '';

    const street = document.getElementById('street').value.trim();
    const note = document.getElementById('note').value.trim();
    const isDefault = document.getElementById('isDefault').checked;

    if (!contactName || !contactPhone || !city || !district || !ward || !street) {
        showAlert('warning', 'Vui lòng nhập đầy đủ thông tin bắt buộc');
        return;
    }

    let url;
    let method;
    let formData;

    if (isEdit) {
        url = `${API_BASE}/address/${addressId}`;
        method = 'PATCH';

        formData = {};

        if (city !== initialAddressData.city) {
            formData.city = city;
        }

        if (district !== initialAddressData.district) {
            formData.district = district;
        }

        if (ward !== initialAddressData.ward) {
            formData.ward = ward;
        }

        if (street !== initialAddressData.street) {
            formData.street = street;
        }

        if (note !== initialAddressData.note) {
            formData.note = note;
        }

        if (isDefault !== initialAddressData.isDefault) {
            formData.isDefault = isDefault;
        }

        if (Object.keys(formData).length === 0) {
            showAlert('info', 'Bạn chưa thay đổi thông tin nào để sửa');
            closeAddressModal();
            return;
        }
    } else {
        url = `${API_BASE}/address`;
        method = 'POST';

        formData = {
            contactName,
            contactPhone,
            city,
            district,
            ward,
            street,
            note: note || null,
            isDefault
        };
    }

    try {
        const response = await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'Không thể lưu địa chỉ');
        }

        showAlert(
            'success',
            isEdit
                ? 'Cập nhật địa chỉ thành công'
                : 'Thêm địa chỉ thành công'
        );

        closeAddressModal();
        await loadAddresses();
    } catch (error) {
        console.error('Lỗi lưu địa chỉ:', error);
        showAlert('danger', error.message || 'Có lỗi xảy ra khi lưu địa chỉ');
    }
}

// Xóa địa chỉ hiện tại
async function deleteCurrentAddress() {
    const addressId = document.getElementById('addressId')?.value;

    if (!addressId) {
        return;
    }

    await deleteAddress(addressId);
}

// Xóa địa chỉ
async function deleteAddress(addressId) {
    if (!confirm('Bạn có chắc chắn muốn xóa địa chỉ này không?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/address/${addressId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const result = await response.json().catch(() => null);
            throw new Error(result?.message || 'Không thể xóa địa chỉ');
        }

        showAlert('success', 'Xóa địa chỉ thành công');

        closeAddressModal();
        await loadAddresses();
    } catch (error) {
        console.error('Lỗi xóa địa chỉ:', error);
        showAlert('danger', error.message || 'Có lỗi xảy ra khi xóa địa chỉ');
    }
}

// Định dạng địa chỉ
function formatAddress(address) {
    return [
        address.street,
        address.ward,
        address.district,
        address.city
    ].filter(Boolean).join(', ');
}

// Escape HTML
function escapeHtml(value) {
    if (value === null || value === undefined) {
        return '';
    }

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Hiển thị thông báo
function showAlert(type, message) {
    const container = document.getElementById('alertContainer');

    if (!container) {
        alert(message);
        return;
    }

    const alertElement = document.createElement('div');

    alertElement.className =
        `alert alert-${type} alert-dismissible fade show shadow-sm rounded-3`;

    alertElement.innerHTML = `
        ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alertElement);

    setTimeout(() => {
        alertElement.remove();
    }, 4000);
}