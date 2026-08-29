package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart_IdOrderByCreatedAtDesc(java.util.UUID cartId);

    Optional<CartItem> findByCart_IdAndFood_Id(java.util.UUID cartId, Long foodId);

    Optional<CartItem> findByIdAndCart_Id(Long id, java.util.UUID cartId);

    void deleteByCart_Id(java.util.UUID cartId);

    void deleteByCart_IdAndFood_Merchant_Id(java.util.UUID cartId, java.util.UUID merchantId);

    List<CartItem> findByCart_IdAndFood_Merchant_Id(java.util.UUID cartId, java.util.UUID merchantId);
}
