package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.User;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByUser(User user);
    Optional<Merchant> findByUserId(Long userId);
    Optional<Merchant> findByUserUsername(String username);
}
