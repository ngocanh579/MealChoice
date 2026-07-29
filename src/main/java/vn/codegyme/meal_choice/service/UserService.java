package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.dto.AddressDTO;
import vn.codegyme.meal_choice.dto.UserDTO;

public interface UserService {

    /**
     * Lấy thông tin profile của user đang đăng nhập
     */
    UserDTO getCurrentUserProfile();

    /**
     * Cập nhật profile (KHÔNG cho phép sửa email và phoneNumber)
     */
    UserDTO updateProfile(UserDTO userDTO);

    /**
     * Thêm địa chỉ mới
     */
    UserDTO addAddress(AddressDTO addressDTO);

    /**
     * Cập nhật địa chỉ đã có
     */
    UserDTO updateAddress(Long addressId, AddressDTO addressDTO);

    /**
     * Xóa địa chỉ
     */
    UserDTO deleteAddress(Long addressId);

    /**
     * Đặt địa chỉ mặc định
     */
    UserDTO setDefaultAddress(Long addressId);
}