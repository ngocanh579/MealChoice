package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Food;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    List<Food> findByMerchant_IdAndDeletedAtIsNull(UUID merchantId);

    List<Food> findByMerchant_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID merchantId);

    List<Food> findByMerchant_IdAndFoodNameContainingIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID merchantId,
            String foodName
    );

    Optional<Food> findByIdAndMerchant_IdAndDeletedAtIsNull(
            UUID foodId,
            UUID merchantId
    );

    boolean existsByIdAndMerchant_IdAndDeletedAtIsNull(
            UUID foodId,
            UUID merchantId
    );
}