package vn.codegyme.meal_choice.service.impl;

import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(
            User user,
            String verificationLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject(
                "Xác nhận tài khoản Trưa Nay Ăn Gì"
        );

        message.setText(
                "Xin chào " + user.getDisplayName() + ",\n\n"
                        + "Cảm ơn bạn đã đăng ký tài khoản.\n"
                        + "Vui lòng nhấn vào liên kết dưới đây để kích hoạt tài khoản:\n\n"
                        + verificationLink
                        + "\n\nLiên kết có hiệu lực trong 24 giờ."
        );

        mailSender.send(message);
    }
}
