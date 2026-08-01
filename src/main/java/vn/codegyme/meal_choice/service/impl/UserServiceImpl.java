package vn.codegyme.meal_choice.service.impl;

import vn.codegyme.meal_choice.dto.request.RegisterRequest;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.EmailService;
import vn.codegyme.meal_choice.service.UserService;
import vn.codegyme.meal_choice.service.VerificationTokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService tokenService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            VerificationTokenService tokenService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email đã được sử dụng."
            );
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        String token =
                tokenService.createVerificationToken(savedUser);

        String verificationLink =
                baseUrl + "/verify-email?token=" + token;

        emailService.sendVerificationEmail(
                savedUser,
                verificationLink
        );
    }
}
