package vn.codegyme.meal_choice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.util.HashSet;

@Component
@Order(1)
@RequiredArgsConstructor
public class AdminAccountSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@mealchoice.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.phone:0999999999}")
    private String adminPhone;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(Role.RoleName.ROLE_ADMIN);
                    return roleRepository.save(role);
                });

        User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User user = new User();
            user.setEmail(adminEmail);
            user.setPhoneNumber(adminPhone);
            user.setDisplayName("MealChoice Admin");
            user.setIsActive(true);
            return user;
        });

        // Luôn đồng bộ với mật khẩu Admin được cấu hình. Trước đây nếu email
        // Admin đã tồn tại với một BCrypt hash khác thì Admin@123 không thể
        // đăng nhập dù đây là tài khoản mẫu được công bố cho dự án.
        if (admin.getPassword() == null
                || !passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
        }
        if (admin.getRoles() == null) {
            admin.setRoles(new HashSet<>());
        }
        admin.getRoles().add(adminRole);
        admin.setIsActive(true);
        userRepository.save(admin);
    }
}
