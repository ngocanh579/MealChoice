package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Food;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    // lấy danh sách món của Merchant
    List<Food> findByMerchant_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID merchantId);

    // lấy một món thuộc Merchant
    Optional<Food> findByIdAndMerchant_IdAndDeletedAtIsNull(
            UUID foodId,
            UUID merchantId
    );

    // kiểm tra món có thuộc Merchant không
    boolean existsByIdAndMerchant_IdAndDeletedAtIsNull(
            UUID foodId,
            UUID merchantId
    );
}