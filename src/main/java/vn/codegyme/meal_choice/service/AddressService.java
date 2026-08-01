package vn.codegyme.meal_choice.service;


import vn.codegyme.meal_choice.entity.Address;

import java.util.List;

public interface AddressService {

    List<Address> getAddressesByUserEmail(String email);

    void deleteAddress(Long addressId, String userEmail);
}
