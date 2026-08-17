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
    boolean existsByMerchantAddress_Id(UUID merchantAddressId);

    // Lấy danh sách món của Merchant
    List<Food> findByMerchant_IdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID merchantId
    );

    // Lấy một món thuộc Merchant
    Optional<Food> findByIdAndMerchant_IdAndDeletedAtIsNull(
            Long foodId,
            UUID merchantId
    );

    // Kiểm tra món có thuộc Merchant không
    boolean existsByIdAndMerchant_IdAndDeletedAtIsNull(
            Long foodId,
            UUID merchantId
    );

    // Lấy tất cả món đang hoạt động
    Page<Food> findAllByIsActiveTrueAndDeletedAtIsNullOrderByIdDesc(
            Pageable pageable
    );

    // Lấy món theo danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND c.id = :categoryId
            ORDER BY f.id DESC
            """)
    Page<Food> findAllByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Tìm kiếm theo tên món
    @Query("""
            SELECT f
            FROM Food f
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY f.id DESC
            """)
    Page<Food> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Tìm kiếm theo tên món và danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND c.id = :categoryId
              AND LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY f.id DESC
            """)
    Page<Food> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Lấy món giảm giá nhiều nhất
    @Query("""
            SELECT f
            FROM Food f
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND f.discountPrice IS NOT NULL
              AND f.discountPrice < f.price
            ORDER BY (f.price - f.discountPrice) DESC, f.id DESC
            """)
    Page<Food> findAllTopDiscounts(
            Pageable pageable
    );

    // Lấy món giảm giá theo danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND c.id = :categoryId
              AND f.discountPrice IS NOT NULL
              AND f.discountPrice < f.price
            ORDER BY (f.price - f.discountPrice) DESC, f.id DESC
            """)
    Page<Food> findTopDiscountsByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Lấy món theo khu vực
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.merchantAddress a
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND LOWER(a.merchantAddress)
                  LIKE LOWER(CONCAT('%', :location, '%'))
            ORDER BY f.id DESC
            """)
    Page<Food> findByLocationNearUser(
            @Param("location") String location,
            Pageable pageable
    );

    // Lấy món theo khu vực và danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.merchantAddress a
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND c.id = :categoryId
              AND LOWER(a.merchantAddress)
                  LIKE LOWER(CONCAT('%', :location, '%'))
            ORDER BY f.id DESC
            """)
    Page<Food> findByLocationNearUserAndCategory(
            @Param("location") String location,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Lấy danh sách món của Merchant đang hoạt động
    Page<Food> findAllByMerchant_IdAndIsActiveTrueAndDeletedAtIsNullOrderByIdDesc(
            UUID merchantId,
            Pageable pageable
    );

    // Lấy danh sách món đang hoạt động trong thành phố
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.merchantAddress a
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND LOWER(a.merchantAddress)
                  LIKE LOWER(CONCAT('%', :city, '%'))
            """)
    List<Food> findAllActiveInCity(
            @Param("city") String city
    );

    // Lấy ID các món user đã thích
    @Query("""
            SELECT f.id
            FROM Food f
            JOIN f.likedByUsers u
            WHERE u.id = :userId
            """)
    List<Long> findLikedFoodIdsByUserId(
            @Param("userId") UUID userId
    );

    // Lấy món mới nhất theo danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND c.id = :categoryId
            ORDER BY f.id DESC
            """)
    List<Food> findLatestFoodByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Lấy món ăn từ đối tác chọn lọc
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.merchant m
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND (m.trustedPartner = true OR m.merchantStatus = vn.codegyme.meal_choice.entity.MerchantStatus.APPROVED)
            ORDER BY f.isRecommended DESC, f.orderCount DESC, f.views DESC, f.id DESC
            """)
    Page<Food> findTrustedFoods(
            Pageable pageable
    );

    // Lấy món đối tác chọn lọc theo danh mục
    @Query("""
            SELECT DISTINCT f
            FROM Food f
            JOIN f.merchant m
            JOIN f.foodCategories c
            WHERE f.isActive = true
              AND f.deletedAt IS NULL
              AND (m.trustedPartner = true OR m.merchantStatus = vn.codegyme.meal_choice.entity.MerchantStatus.APPROVED)
              AND c.id = :categoryId
            ORDER BY f.isRecommended DESC, f.orderCount DESC, f.views DESC, f.id DESC
            """)
    Page<Food> findTrustedFoodsByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}