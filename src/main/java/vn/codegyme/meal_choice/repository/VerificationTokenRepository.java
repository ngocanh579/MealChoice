package vn.codegyme.meal_choice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegyme.meal_choice.entity.VerificationToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}