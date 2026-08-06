package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.entity.VerificationToken;

@Service
@RequiredArgsConstructor
public class AccountActivationEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountActivationEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendActivationEmail(User user, VerificationToken verificationToken) {
        String activationUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/account/verify")
                .queryParam("token", verificationToken.getToken())
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Kích hoạt tài khoản MealChoice");
        message.setText(
                "Xin chào " + user.getDisplayName() + ",\n\n" +
                        "Cảm ơn bạn đã đăng ký MealChoice. Hãy mở liên kết dưới đây để kích hoạt tài khoản:\n" +
                        activationUrl + "\n\n" +
                        "Liên kết có hiệu lực trong 24 giờ. Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này."
        );

        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            // Không làm mất tài khoản vừa đăng ký khi máy phát triển chưa cấu hình SMTP.
            LOGGER.error("Không thể gửi email kích hoạt cho {}", user.getEmail(), exception);
        }
    }
}
