package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class EmailService {
    private final JavaMailSender mailSender;

    public void sendMerchantRegisterEmail(String email, String restaurantName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Đăng ký Merchant thành công");
        message.setText(
                "Xin chào,\n\n" +
                        "Nhà hàng " + restaurantName +
                        " đã đăng ký Merchant thành công trên hệ thống Trưa Nay Ăn Gì.\n\n" +
                        "Hồ sơ của bạn đang chờ Admin xét duyệt.\n\n" +
                        "Trân trọng."
        );

        mailSender.send(message);
    }
}
