package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.dto.AuthResponse;
import vn.codegyme.meal_choice.dto.RegisterRequest;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest registerRequest){

        // Check email if it exists
        if (userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email đã tồn tại");
        }

        // Create a new user with hash password
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setDisplayName(registerRequest.getDisplayName());

        // Save in database
        userRepository.save(user);

        // Create toke for response
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getAvatarUrl()
        );
    }
}
