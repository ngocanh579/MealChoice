package vn.codegyme.meal_choice.service;


import vn.codegyme.meal_choice.entity.Address;

import java.util.List;

public interface AddressService {

    /**
     * Lấy danh sách địa chỉ của người đang đăng nhập.
     */
    List<Address> getAddressesByUserEmail(String userEmail);

    /**
     * Xóa địa chỉ của người đang đăng nhập.
     */
    void deleteAddress(
            Long addressId,
            String userEmail
    );
}
