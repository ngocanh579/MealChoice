package vn.codegyme.meal_choice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Lấy danh sách địa chỉ của một người dùng.
     */
    List<Address> findByUserId(UUID userId);

    /**
     * Lấy danh sách địa chỉ, ưu tiên địa chỉ mặc định lên trước.
     */
    List<Address> findByUserIdOrderByIsDefaultDescIdDesc(UUID userId);

    /**
     * Tìm địa chỉ theo ID nhưng phải thuộc đúng người dùng.
     */
    Optional<Address> findByIdAndUserId(
            Long addressId,
            UUID userId
    );

    /**
     * Lấy một địa chỉ còn lại để đặt làm mặc định.
     */
    Optional<Address> findFirstByUserIdOrderByIdAsc(UUID userId);
}
