package vn.codegyme.meal_choice.repository;


import vn.codegyme.meal_choice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findAllByUserEmailOrderByDefaultAddressDesc(
            String email
    );

    Optional<Address> findByIdAndUserEmail(
            Long addressId,
            String email
    );

    long countByUserEmail(String email);
}
