package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.dto.AuthResponse;
import vn.codegyme.meal_choice.dto.LoginRequest;
import vn.codegyme.meal_choice.dto.RegisterRequest;
import vn.codegyme.meal_choice.entity.Role;
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

        // Check phone number if it exists
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())){
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        // Check duplicate password
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Create a new user with hash password
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDisplayName(registerRequest.getDisplayName());
        user.setRole(Role.USER);

        // Save in database
        userRepository.save(user);

        // Create toke for response
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setDisplayName(user.getDisplayName());
        authResponse.setPhoneNumber(user.getPhoneNumber());
        authResponse.setAvatarUrl(user.getAvatarUrl());
        authResponse.setRole(user.getRole().name());

        return authResponse;
    }

    public AuthResponse login (LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setDisplayName(user.getDisplayName());
        authResponse.setAvatarUrl(user.getAvatarUrl());
        authResponse.setRole(user.getRole().name());

        return authResponse;
    }
}
