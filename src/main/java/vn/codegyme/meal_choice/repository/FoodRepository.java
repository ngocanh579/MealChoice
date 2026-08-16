package vn.codegyme.meal_choice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Food;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    // 1. Lấy tất cả món ăn đang hoạt động theo ID giảm dần
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL ORDER BY f.id DESC")
    Page<Food> findAllByIsActiveTrueOrderByIdDesc(Pageable pageable);

    // 2. Lấy món ăn theo danh mục FoodCategory
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.category.id = :categoryId ORDER BY f.id DESC")
    Page<Food> findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    // 3. Tìm kiếm theo từ khóa (tên món hoặc địa chỉ)
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.id DESC")
    Page<Food> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 4. Tìm kiếm theo cả từ khóa và danh mục Category
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.category.id = :categoryId AND (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.id DESC")
    Page<Food> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);

    // 5. Danh sách Top món giảm giá nhiều nhất
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.discountPrice IS NOT NULL AND f.discountPrice < f.price ORDER BY (f.price - f.discountPrice) DESC, f.id DESC")
    Page<Food> findAllTopDiscounts(Pageable pageable);

    // 6. Danh sách Top món giảm giá nhiều nhất theo Category
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.category.id = :categoryId AND f.discountPrice IS NOT NULL AND f.discountPrice < f.price ORDER BY (f.price - f.discountPrice) DESC, f.id DESC")
    Page<Food> findTopDiscountsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    // 7. Lấy danh sách món theo vị trí gần khách hàng
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND (LOWER(f.address) LIKE LOWER(CONCAT('%', :location, '%'))) ORDER BY f.id DESC")
    Page<Food> findByLocationNearUser(@Param("location") String location, Pageable pageable);

    // 8. Lấy danh sách món theo vị trí gần khách hàng và danh mục
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.category.id = :categoryId AND (LOWER(f.address) LIKE LOWER(CONCAT('%', :location, '%'))) ORDER BY f.id DESC")
    Page<Food> findByLocationNearUserAndCategory(@Param("location") String location, @Param("categoryId") Long categoryId, Pageable pageable);

    // 9. Lấy danh sách món của Merchant
    List<Food> findByMerchant_IdAndDeletedAtIsNull(UUID merchantId);

    // 10. Lấy một món thuộc Merchant
    Optional<Food> findByIdAndMerchant_IdAndDeletedAtIsNull(Long foodId, UUID merchantId);

    // 11. Tìm món ăn của một merchant cụ thể
    @Query("SELECT f FROM Food f WHERE f.merchant.id = :merchantId AND f.active = true AND f.deletedAt IS NULL ORDER BY f.id DESC")
    Page<Food> findAllByMerchant_IdAndIsActiveTrueOrderByIdDesc(@Param("merchantId") UUID merchantId, Pageable pageable);

    // 12. Lấy danh sách món ăn active cùng thành phố (dựa trên địa chỉ của Merchant)
    @Query("SELECT f FROM Food f JOIN f.merchant m JOIN m.addresses a WHERE f.active = true AND f.deletedAt IS NULL AND LOWER(a.merchantAddress) LIKE LOWER(CONCAT('%', :city))")
    List<Food> findAllActiveInCity(@Param("city") String city);

    // 13. Lấy danh sách ID món ăn mà một User (dựa trên userId) đã thích
    @Query("SELECT f.id FROM Food f JOIN f.likedByUsers u WHERE u.id = :userId")
    List<Long> findLikedFoodIdsByUserId(@Param("userId") UUID userId);

    // 14. Lấy món ăn mới nhất thuộc một danh mục cụ thể
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.category.id = :categoryId ORDER BY f.id DESC LIMIT 1")
    Optional<Food> findLatestFoodByCategoryId(@Param("categoryId") Long categoryId);

    // 15. Lấy danh sách món ăn từ các đối tác chọn lọc (Trusted Partners)
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.merchant.trustedPartner = true ORDER BY f.id DESC")
    Page<Food> findAllByTrustedPartner(Pageable pageable);

    // 16. Lấy danh sách món ăn từ các đối tác chọn lọc theo danh mục
    @Query("SELECT f FROM Food f WHERE f.active = true AND f.deletedAt IS NULL AND f.merchant.trustedPartner = true AND f.category.id = :categoryId ORDER BY f.id DESC")
    Page<Food> findAllByTrustedPartnerAndCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}
