package vn.codegyme.meal_choice.repository;

import vn.codegyme.meal_choice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
