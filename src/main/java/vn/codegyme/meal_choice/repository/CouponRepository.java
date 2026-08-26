package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.codegyme.meal_choice.entity.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findAllByMerchant_IdOrderByCreatedAtDesc(UUID merchantId);
    // lọc coupon còn hiệu lực
    List<Coupon> findAllByMerchant_IdAndIsActiveTrueOrderByCreatedAtDesc(UUID merchantId);

    Optional<Coupon> findByIdAndMerchant_Id(Long id, UUID merchantId);

    List<Coupon> findAllByIdInAndMerchant_Id(
            List<Long> ids,
            UUID merchantId
    );

    boolean existsByMerchant_IdAndCouponCode(
            UUID merchantId,
            String couponCode
    );

    boolean existsByMerchant_IdAndCouponCodeAndIdNot(
            UUID merchantId,
            String couponCode,
            Long id
    );
}