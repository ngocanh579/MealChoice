package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.entity.VerificationToken;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final AccountActivationEmailService emailService;

    @Transactional
    public void createAndSendToken(User user) {
        verificationTokenRepository.deleteByUser(user);
        verificationTokenRepository.flush();

        user.setIsActive(false);
        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        emailService.sendActivationEmail(user, verificationToken);
    }

    @Transactional
    public String activate(String tokenValue) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Liên kết kích hoạt không hợp lệ"));

        if (verificationToken.isVerified()) {
            return verificationToken.getUser().getEmail();
        }

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Liên kết kích hoạt đã hết hạn");
        }

        User user = verificationToken.getUser();
        user.setIsActive(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());

        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);
        return user.getEmail();
    }
}
