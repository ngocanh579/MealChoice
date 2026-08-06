package vn.codegyme.meal_choice.service;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.AuthResponse;
import vn.codegyme.meal_choice.dto.LoginRequest;
import vn.codegyme.meal_choice.dto.RegisterRequest;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.RefreshTokenRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

@Service
@Primary
public class ActivationAuthService extends AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountActivationService accountActivationService;

    public ActivationAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            AccountActivationService accountActivationService
    ) {
        super(userRepository, passwordEncoder, jwtService, roleRepository, refreshTokenRepository);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountActivationService = accountActivationService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        AuthResponse response = super.register(registerRequest);
        User user = userRepository.findByEmail(registerRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản vừa đăng ký"));

        refreshTokenRepository.deleteByUser(user);
        accountActivationService.createAndSendToken(user);

        // Chỉ cấp token đăng nhập sau khi người dùng xác nhận email.
        response.setAccessToken(null);
        response.setRefreshToken(null);
        return response;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt hoặc đang bị khóa");
        }

        return super.login(loginRequest);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        User user = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"))
                .getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            refreshTokenRepository.deleteByToken(refreshTokenValue);
            throw new RuntimeException("Tài khoản chưa được kích hoạt hoặc đang bị khóa");
        }
        return super.refreshToken(refreshTokenValue);
    }
}
