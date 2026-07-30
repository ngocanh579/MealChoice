package vn.codegyme.meal_choice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.codegyme.meal_choice.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
}
