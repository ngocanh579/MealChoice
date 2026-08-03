package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.dto.AuthResponse;
import vn.codegyme.meal_choice.dto.LoginRequest;
import vn.codegyme.meal_choice.dto.RegisterRequest;
import vn.codegyme.meal_choice.entity.RefreshToken;
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.RefreshTokenRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L;

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

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role USER chưa được khởi tạo trong hệ thống"));


        // Create a new user with hash password
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDisplayName(registerRequest.getDisplayName());
        user.setRoles(new HashSet<>(Set.of(userRole)));

        // Save in database
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login (LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        return buildAuthResponse(user);
    }

    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        refreshTokenRepository.delete(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user){

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getEmail());

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusNanos(REFRESH_TOKEN_EXPIRATION_MS * 1_000_000));
        refreshTokenRepository.save(refreshToken);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshTokenValue);
        authResponse.setId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setDisplayName(user.getDisplayName());
        authResponse.setPhoneNumber(user.getPhoneNumber());
        authResponse.setAvatarUrl(user.getAvatarUrl());
        authResponse.setRoles(
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );

        return authResponse;
    }
}
