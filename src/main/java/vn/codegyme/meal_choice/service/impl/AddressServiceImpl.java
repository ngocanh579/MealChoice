package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.dto.AddressResponseDTO;
import vn.codegyme.meal_choice.service.AddressService;
import vn.codegyme.meal_choice.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserService userService;

    @Override
    public List<AddressResponseDTO> findCurrentUserAddresses() {
        return userService.getAllAddresses();
    }

    @Override
    public AddressResponseDTO findCurrentUserAddress(Long addressId) {
        return userService.getAddressById(addressId);
    }
}
