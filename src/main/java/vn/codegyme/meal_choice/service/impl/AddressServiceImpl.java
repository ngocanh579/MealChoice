package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.repository.AddressRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.AddressService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUserEmail(
            String userEmail
    ) {
        User user = getUserByEmail(userEmail);

        return addressRepository
                .findByUserIdOrderByIsDefaultDescIdDesc(
                        user.getId()
                );
    }

    @Override
    @Transactional
    public void deleteAddress(
            Long addressId,
            String userEmail
    ) {
        User user = getUserByEmail(userEmail);

        UUID userId = user.getId();

        /*
         * Chỉ lấy địa chỉ khi địa chỉ đó thuộc người đang đăng nhập.
         */
        Address address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy địa chỉ hoặc bạn không có quyền xóa địa chỉ này."
                        )
                );

        boolean isDeletingDefaultAddress =
                Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        /*
         * Thực hiện DELETE ngay trước khi truy vấn địa chỉ còn lại.
         */
        addressRepository.flush();

        /*
         * Nếu xóa địa chỉ mặc định thì chọn một địa chỉ khác
         * làm mặc định.
         */
        if (isDeletingDefaultAddress) {
            setAnotherAddressAsDefault(userId);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy tài khoản người dùng."
                        )
                );
    }

    private void setAnotherAddressAsDefault(UUID userId) {
        addressRepository
                .findFirstByUserIdOrderByIdAsc(userId)
                .ifPresent(address -> {
                    address.setIsDefault(true);
                    addressRepository.save(address);
                });
    }
}