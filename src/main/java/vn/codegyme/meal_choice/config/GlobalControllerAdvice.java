package vn.codegyme.meal_choice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${app.footer.company-name:Trưa Nay Ăn Gì}")
    private String companyName;

    @Value("${app.footer.address:Số 1 Đại Cồ Việt, Hà Nội}")
    private String address;

    @Value("${app.footer.phone:1900 1234}")
    private String phone;

    @Value("${app.footer.email:support@truanayangi.com}")
    private String email;

    @Value("${app.footer.copyright:© 2026 Trưa Nay Ăn Gì}")
    private String copyright;

    @ModelAttribute("footerInfo")
    public Map<String, String> getFooterInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("companyName", companyName);
        info.put("address", address);
        info.put("phone", phone);
        info.put("email", email);
        info.put("copyright", copyright);
        return info;
    }
}
