package vn.codegyme.meal_choice.service.impl;

import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.entity.VerificationToken;
import vn.codegyme.meal_choice.enums.VerificationResult;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.repository.VerificationTokenRepository;
import vn.codegyme.meal_choice.service.VerificationTokenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenServiceImpl
        implements VerificationTokenService {

    private static final int EXPIRATION_HOURS = 24;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerificationTokenServiceImpl(
            VerificationTokenRepository tokenRepository,
            UserRepository userRepository) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String createVerificationToken(User user) {

        /*
         * Mỗi user chỉ giữ một token xác nhận.
         * Nếu gửi lại email thì token cũ sẽ bị xóa.
         */
        tokenRepository.findByUserId(user.getId())
                .ifPresent(tokenRepository::delete);

        String tokenValue = UUID.randomUUID().toString();

        VerificationToken verificationToken =
                new VerificationToken();

        verificationToken.setToken(tokenValue);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(
                LocalDateTime.now().plusHours(EXPIRATION_HOURS)
        );
        verificationToken.setUsed(false);

        tokenRepository.save(verificationToken);

        return tokenValue;
    }

    @Override
    @Transactional
    public VerificationResult verifyToken(String tokenValue) {

        if (tokenValue == null || tokenValue.isBlank()) {
            return VerificationResult.TOKEN_NOT_FOUND;
        }

        Optional<VerificationToken> optionalToken =
                tokenRepository.findByToken(tokenValue);

        if (optionalToken.isEmpty()) {
            return VerificationResult.TOKEN_NOT_FOUND;
        }

        VerificationToken verificationToken =
                optionalToken.get();

        if (verificationToken.isUsed()) {
            return VerificationResult.TOKEN_ALREADY_USED;
        }

        User user = verificationToken.getUser();

        if (user.isEnabled()) {
            return VerificationResult.USER_ALREADY_ENABLED;
        }

        if (verificationToken.isExpired()) {
            return VerificationResult.TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        return VerificationResult.SUCCESS;
    }
}
