package vn.codegyme.meal_choice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Food;

import java.util.UUID;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    // 1. Lấy tất cả món ăn đang hoạt động theo ID giảm dần
    @Query("SELECT f FROM Food f WHERE f.active = true ORDER BY f.id DESC")
    Page<Food> findAllByIsActiveTrueOrderByIdDesc(Pageable pageable);

    // 2. Lấy món ăn theo danh mục Category
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.category.id = :categoryId ORDER BY f.id DESC")
    Page<Food> findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    // 3. Tìm kiếm theo từ khóa (tên món hoặc địa chỉ)
    @Query("SELECT f FROM Food f WHERE f.active = true AND (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.id DESC")
    Page<Food> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 4. Tìm kiếm theo cả từ khóa và danh mục Category
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.category.id = :categoryId AND (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.id DESC")
    Page<Food> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);

    // 5. Danh sách Top món giảm giá nhiều nhất
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.discountPrice IS NOT NULL AND f.discountPrice < f.price ORDER BY (f.price - f.discountPrice) DESC, f.id DESC")
    Page<Food> findAllTopDiscounts(Pageable pageable);

    // 6. Danh sách Top món giảm giá nhiều nhất theo Category
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.category.id = :categoryId AND f.discountPrice IS NOT NULL AND f.discountPrice < f.price ORDER BY (f.price - f.discountPrice) DESC, f.id DESC")
    Page<Food> findTopDiscountsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    // 7. Lấy danh sách món theo vị trí gần khách hàng
    @Query("SELECT f FROM Food f WHERE f.active = true AND (LOWER(f.address) LIKE LOWER(CONCAT('%', :location, '%'))) ORDER BY f.id DESC")
    Page<Food> findByLocationNearUser(@Param("location") String location, Pageable pageable);

    // 8. Lấy danh sách món theo vị trí gần khách hàng và danh mục
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.category.id = :categoryId AND (LOWER(f.address) LIKE LOWER(CONCAT('%', :location, '%'))) ORDER BY f.id DESC")
    Page<Food> findByLocationNearUserAndCategory(@Param("location") String location, @Param("categoryId") Long categoryId, Pageable pageable);

    // 9. Tìm món ăn của một merchant cụ thể
    @Query("SELECT f FROM Food f WHERE f.merchant.id = :merchantId AND f.active = true ORDER BY f.id DESC")
    Page<Food> findAllByMerchant_IdAndIsActiveTrueOrderByIdDesc(@Param("merchantId") UUID merchantId, Pageable pageable);

    // 10. Lấy danh sách món ăn active cùng thành phố (dựa trên địa chỉ của Merchant)
    @Query("SELECT f FROM Food f JOIN f.merchant m JOIN m.addresses a WHERE f.active = true AND LOWER(a.merchantAddress) LIKE LOWER(CONCAT('%', :city))")
    List<Food> findAllActiveInCity(@Param("city") String city);

    // 11. Lấy danh sách ID món ăn mà một User (dựa trên userId) đã thích
    @Query("SELECT f.id FROM Food f JOIN f.likedByUsers u WHERE u.id = :userId")
    List<Long> findLikedFoodIdsByUserId(@Param("userId") UUID userId);
}
