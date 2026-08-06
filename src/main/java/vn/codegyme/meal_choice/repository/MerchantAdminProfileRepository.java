package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.MerchantAdminProfile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantAdminProfileRepository extends JpaRepository<MerchantAdminProfile, Long> {

    Optional<MerchantAdminProfile> findByMerchantId(UUID merchantId);
}
