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
}
