package vn.codegyme.meal_choice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByMerchant_IdOrderByIdDesc(UUID merchantId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByMerchant_IdAndStatusOrderByIdDesc(UUID merchantId, OrderStatus status);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByMerchant_IdOrderByIdDesc(UUID merchantId, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByMerchant_IdAndStatusOrderByIdDesc(UUID merchantId, OrderStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Optional<Order> findByIdAndMerchant_Id(Long id, UUID merchantId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Optional<Order> findByOrderCode(String orderCode);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    List<Order> findByUser_IdOrderByIdDesc(UUID userId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderItems", "merchant", "user"})
    Page<Order> findByUser_IdOrderByIdDesc(UUID userId, Pageable pageable);

    long countByMerchant_IdAndStatus(UUID merchantId, OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.food WHERE o.id = :id AND o.merchant.id = :merchantId")
    Optional<Order> findByIdAndMerchantIdWithItems(@Param("id") Long id, @Param("merchantId") UUID merchantId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.food WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithItems(@Param("orderCode") String orderCode);
}
