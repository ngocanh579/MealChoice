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
        sendEmail(
                email,
                "Đăng ký Merchant thành công",
                "Xin chào,\n\n" +
                        "Nhà hàng " + restaurantName +
                        " đã đăng ký Merchant thành công trên hệ thống Trưa Nay Ăn Gì.\n\n" +
                        "Hồ sơ của bạn đang chờ Admin xét duyệt.\n\n" +
                        "Trân trọng."
        );
    }

    public void sendTrustedPartnerRegistrationEmail(
            String email,
            String restaurantName) {

        sendEmail(
                email,
                "Đăng ký đối tác thân thiết thành công",
                "Xin chào,\n\n" +
                        "Nhà hàng " + restaurantName +
                        " đã đăng ký trở thành đối tác thân thiết trên hệ thống Trưa Nay Ăn Gì.\n\n" +
                        "Hồ sơ đăng ký của bạn đang chờ Admin xét duyệt.\n\n" +
                        "Vui lòng chờ thông báo từ hệ thống sau khi Admin hoàn tất xét duyệt.\n\n" +
                        "Trân trọng."
        );
    }

    public void sendTrustedPartnerApprovedEmail(
            String email,
            String restaurantName) {

        sendEmail(
                email,
                "Đăng ký đối tác thân thiết được phê duyệt",
                "Xin chào,\n\n" +
                        "Chúc mừng nhà hàng " + restaurantName +
                        " đã được Admin phê duyệt trở thành đối tác thân thiết trên hệ thống Trưa Nay Ăn Gì.\n\n" +
                        "Từ bây giờ, nhà hàng của bạn chính thức là đối tác thân thiết.\n\n" +
                        "Trân trọng."
        );
    }

    public void sendTrustedPartnerRejectedEmail(
            String email,
            String restaurantName,
            String reason) {

        sendEmail(
                email,
                "Đăng ký đối tác thân thiết bị từ chối",
                "Xin chào,\n\n" +
                        "Đăng ký trở thành đối tác thân thiết của nhà hàng " +
                        restaurantName +
                        " chưa được Admin phê duyệt.\n\n" +
                        "Lý do: " + reason + "\n\n" +
                        "Bạn có thể đăng ký lại sau khi đáp ứng đủ điều kiện.\n\n" +
                        "Trân trọng."
        );
    }

    private void sendEmail(
            String email,
            String subject,
            String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}