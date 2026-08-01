package vn.codegyme.meal_choice.service.impl;

import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.repository.AddressRepository;
import vn.codegyme.meal_choice.service.AddressService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl
        implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(
            AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> getAddressesByUserEmail(
            String email) {

        return addressRepository
                .findAllByUserEmailOrderByDefaultAddressDesc(
                        email
                );
    }

    @Override
    @Transactional
    public void deleteAddress(
            Long addressId,
            String userEmail) {

        Address address = addressRepository
                .findByIdAndUserEmail(
                        addressId,
                        userEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy địa chỉ hoặc "
                                        + "bạn không có quyền xóa địa chỉ này."
                        )
                );

        /*
         * Có thể cho phép xóa địa chỉ mặc định.
         * Sau khi xóa, hệ thống chọn địa chỉ còn lại
         * làm mặc định.
         */
        boolean wasDefaultAddress =
                address.isDefaultAddress();

        addressRepository.delete(address);

        if (wasDefaultAddress) {
            setAnotherAddressAsDefault(userEmail);
        }
    }

    private void setAnotherAddressAsDefault(
            String userEmail) {

        List<Address> remainingAddresses =
                addressRepository
                        .findAllByUserEmailOrderByDefaultAddressDesc(
                                userEmail
                        );

        if (!remainingAddresses.isEmpty()) {
            Address newDefaultAddress =
                    remainingAddresses.get(0);

            newDefaultAddress.setDefaultAddress(true);

            addressRepository.save(newDefaultAddress);
        }
    }
}
