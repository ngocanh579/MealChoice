package vn.codegyme.meal_choice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Order;
import vn.codegyme.meal_choice.entity.OrderStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Order> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUser_Id(UUID id, UUID userId);

    boolean existsByOrderCode(String orderCode);
}
