package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.codegyme.meal_choice.event.UserRegisteredEvent;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(vn.codegyme.meal_choice.service.EmailService.class);

    private final JavaMailSender mailSender;

    /**
     * Lắng nghe UserRegisteredEvent và chỉ gửi mail SAU KHI transaction đăng ký
     * (AuthService#register) đã commit thành công (AFTER_COMMIT).
     * -> Nếu đăng ký thất bại/rollback (vd trùng email) thì sẽ KHÔNG gửi mail.
     * -> Đồng thời @Async đảm bảo việc gửi mail chạy trên thread riêng
     *    (mailTaskExecutor khai báo trong MailConfig), không làm chậm response
     *    trả về cho client.
     */
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        sendUserRegisterEmail(event.email(), event.displayName());
    }

    @Async("mailTaskExecutor")
    public void sendMerchantRegisterEmail(String email, String restaurantName) {
        try {
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
        } catch (MailException ex) {
            log.error("Gửi email xác nhận Merchant tới '{}' thất bại: {}", email, ex.getMessage(), ex);
        }
    }

    public void sendUserRegisterEmail(String email, String displayName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);
            message.setSubject("Đăng ký tài khoản thành công");
            message.setText(
                    "Xin chào " + displayName + ",\n\n" +
                            "Bạn đã đăng ký tài khoản thành công trên hệ thống Trưa Nay Ăn Gì.\n\n" +
                            "Email đăng nhập: " + email + "\n\n" +
                            "Trân trọng."
            );

            mailSender.send(message);
            log.info("Đã gửi email xác nhận đăng ký tới '{}'", email);
        } catch (MailException ex) {
            // Không throw lại: đây là tác vụ phụ, không được phép ảnh hưởng
            // tới luồng nghiệp vụ chính (đăng ký tài khoản đã commit xong rồi).
            log.error("Gửi email xác nhận đăng ký tới '{}' thất bại: {}", email, ex.getMessage(), ex);
        }
    }
}
