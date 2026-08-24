package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Order;
import vn.codegyme.meal_choice.entity.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMerchant_IdOrderByCreatedAtDesc(UUID merchantId);

    List<Order> findByMerchant_IdAndStatusOrderByCreatedAtDesc(UUID merchantId, OrderStatus status);

    Optional<Order> findByIdAndMerchant_Id(Long id, UUID merchantId);

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    long countByMerchant_IdAndStatus(UUID merchantId, OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.food WHERE o.id = :id AND o.merchant.id = :merchantId")
    Optional<Order> findByIdAndMerchantIdWithItems(@Param("id") Long id, @Param("merchantId") UUID merchantId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.food WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithItems(@Param("orderCode") String orderCode);

    // 1. THỐNG KÊ DOANH THU
    @Query(value = """
        SELECT 
            CONCAT('Tháng ', MONTH(created_at), '/', YEAR(created_at)) AS period,
            COUNT(id) AS totalOrders,
            COALESCE(SUM(total_amount), 0) AS totalRevenue
        FROM orders
        WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR) AND status = 'COMPLETED'
        GROUP BY period
        """, nativeQuery = true)
    List<Object[]> findRevenueStatsByMonth(@Param("merchantId") String merchantId);

    @Query(value = """
        SELECT 
            CONCAT('Tuần ', WEEK(created_at), '/', YEAR(created_at)) AS period,
            COUNT(id) AS totalOrders,
            COALESCE(SUM(total_amount), 0) AS totalRevenue
        FROM orders
        WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR) AND status = 'COMPLETED'
        GROUP BY period
        """, nativeQuery = true)
    List<Object[]> findRevenueStatsByWeek(@Param("merchantId") String merchantId);

    @Query(value = """
        SELECT 
            CONCAT('Quý ', QUARTER(created_at), '/', YEAR(created_at)) AS period,
            COUNT(id) AS totalOrders,
            COALESCE(SUM(total_amount), 0) AS totalRevenue
        FROM orders
        WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR) AND status = 'COMPLETED'
        GROUP BY period
        """, nativeQuery = true)
    List<Object[]> findRevenueStatsByQuarter(@Param("merchantId") String merchantId);

    // 2. THỐNG KÊ KHÁCH HÀNG
    @Query(value = """
        SELECT 
            contact_name AS customerName,
            contact_phone AS customerPhone,
            COUNT(id) AS totalOrders,
            COALESCE(SUM(total_amount), 0) AS totalSpent
        FROM orders
        WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR) AND status = 'COMPLETED'
        GROUP BY contact_name, contact_phone
        ORDER BY totalSpent DESC
        """, nativeQuery = true)
    List<Object[]> findCustomerStatsByMerchant(@Param("merchantId") String merchantId);

    // 3. THỐNG KÊ COUPON
    @Query(value = """
        SELECT 
            COUNT(id) AS totalOrdersWithDiscount,
            COALESCE(SUM(discount_amount), 0) AS totalDiscountAmount,
            COALESCE(SUM(total_amount), 0) AS totalRevenue
        FROM orders
        WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR) AND status = 'COMPLETED'
        """, nativeQuery = true)
    List<Object[]> findCouponStatsByMerchant(@Param("merchantId") String merchantId);

    // 4. THỐNG KÊ MÓN ĂN (BÁN CHẠY) - Đã gộp theo tên món
    @Query(value = """
        SELECT 
            oi.food_name AS foodName,
            COALESCE(SUM(oi.quantity), 0) AS totalQuantity,
            COALESCE(SUM(oi.subtotal), 0) AS totalRevenue
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE CAST(o.merchant_id AS CHAR) = CAST(:merchantId AS CHAR) 
          AND o.status = 'COMPLETED'
        GROUP BY oi.food_name
        ORDER BY totalQuantity DESC
        """, nativeQuery = true)
    List<Object[]> findFoodStatsByMerchant(@Param("merchantId") String merchantId);
}