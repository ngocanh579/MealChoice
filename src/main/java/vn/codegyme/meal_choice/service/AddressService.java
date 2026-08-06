package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.dto.AddressResponseDTO;

import java.util.List;

public interface AddressService {

    List<AddressResponseDTO> findCurrentUserAddresses();

    AddressResponseDTO findCurrentUserAddress(Long addressId);
}
