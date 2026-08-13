package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.FoodCategory;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodCategoryRepository
        extends JpaRepository<FoodCategory, UUID> {

    // tìm category theo tên
    Optional<FoodCategory> findByCategoryName(String categoryName);

    // kiểm tra category đã tồn tại
    boolean existsByCategoryName(String categoryName);
}