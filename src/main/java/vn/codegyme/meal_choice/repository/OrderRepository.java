package vn.codegyme.meal_choice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByMerchant_IdOrderByIdDesc(UUID merchantId);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByMerchant_IdAndStatusOrderByIdDesc(UUID merchantId, OrderStatus status);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByMerchant_IdOrderByIdDesc(UUID merchantId, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByMerchant_IdAndStatusOrderByIdDesc(UUID merchantId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Optional<Order> findByIdAndMerchant_Id(Long id, UUID merchantId);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Optional<Order> findByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByUser_IdOrderByIdDesc(UUID userId);

    @EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByUser_IdOrderByIdDesc(UUID userId, Pageable pageable);

    long countByMerchant_IdAndStatus(UUID merchantId, OrderStatus status);

    @Query("""
            SELECT o
            FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.food
            WHERE o.id = :id
              AND o.merchant.id = :merchantId
            """)
    Optional<Order> findByIdAndMerchantIdWithItems(
            @Param("id") Long id,
            @Param("merchantId") UUID merchantId
    );

    @Query("""
            SELECT o
            FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.food
            WHERE o.orderCode = :orderCode
            """)
    Optional<Order> findByOrderCodeWithItems(@Param("orderCode") String orderCode);

    @Query(value = """
            SELECT
                CONCAT('Tháng ', MONTH(created_at), '/', YEAR(created_at)) AS period,
                COUNT(id) AS totalOrders,
                COALESCE(SUM(total_amount), 0) AS totalRevenue
            FROM orders
            WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            GROUP BY period
            """, nativeQuery = true)
    List<Object[]> findRevenueStatsByMonth(@Param("merchantId") String merchantId);

    @Query(value = """
            SELECT
                CONCAT('Tuần ', WEEK(created_at), '/', YEAR(created_at)) AS period,
                COUNT(id) AS totalOrders,
                COALESCE(SUM(total_amount), 0) AS totalRevenue
            FROM orders
            WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            GROUP BY period
            """, nativeQuery = true)
    List<Object[]> findRevenueStatsByWeek(@Param("merchantId") String merchantId);

    @Query(value = """
            SELECT
                CONCAT('Quý ', QUARTER(created_at), '/', YEAR(created_at)) AS period,
                COUNT(id) AS totalOrders,
                COALESCE(SUM(total_amount), 0) AS totalRevenue
            FROM orders
            WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            GROUP BY period
            """, nativeQuery = true)
    List<Object[]> findRevenueStatsByQuarter(@Param("merchantId") String merchantId);

    @Query(value = """
            SELECT
                contact_name AS customerName,
                contact_phone AS customerPhone,
                COUNT(id) AS totalOrders,
                COALESCE(SUM(total_amount), 0) AS totalSpent
            FROM orders
            WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            GROUP BY contact_name, contact_phone
            ORDER BY totalOrders DESC, totalSpent DESC
            """, nativeQuery = true)
    List<Object[]> findCustomerStatsByMerchant(@Param("merchantId") String merchantId);

    @Query(value = """
            SELECT
                COUNT(id) AS totalOrdersWithDiscount,
                COALESCE(SUM(discount_amount), 0) AS totalDiscountAmount,
                COALESCE(SUM(total_amount), 0) AS totalRevenue
            FROM orders
            WHERE CAST(merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            """, nativeQuery = true)
    List<Object[]> findCouponStatsByMerchant(@Param("merchantId") String merchantId);

    @Query(value = """
            SELECT
                TRIM(oi.food_name) AS foodName,
                COALESCE(SUM(oi.quantity), 0) AS totalQuantity,
                COALESCE(SUM(oi.subtotal), 0) AS totalRevenue
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE CAST(o.merchant_id AS CHAR) = CAST(:merchantId AS CHAR)
              AND o.status IN ('PREPARING', 'DELIVERING', 'COMPLETED')
            GROUP BY TRIM(oi.food_name)
            ORDER BY totalQuantity DESC
            """, nativeQuery = true)
    List<Object[]> findFoodStatsByMerchant(@Param("merchantId") String merchantId);
}