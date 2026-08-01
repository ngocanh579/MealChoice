package vn.codegyme.meal_choice.controller;

import vn.codegyme.meal_choice.enums.VerificationResult;
import vn.codegyme.meal_choice.service.VerificationTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailVerificationController {

    private final VerificationTokenService tokenService;

    public EmailVerificationController(
            VerificationTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam("token") String token,
            Model model) {

        VerificationResult result =
                tokenService.verifyToken(token);

        switch (result) {
            case SUCCESS -> {
                model.addAttribute(
                        "success",
                        true
                );
                model.addAttribute(
                        "message",
                        "Tài khoản đã được kích hoạt thành công."
                );
            }

            case TOKEN_EXPIRED -> {
                model.addAttribute(
                        "success",
                        false
                );
                model.addAttribute(
                        "message",
                        "Liên kết xác nhận đã hết hạn."
                );
            }

            case TOKEN_ALREADY_USED,
                 USER_ALREADY_ENABLED -> {
                model.addAttribute(
                        "success",
                        true
                );
                model.addAttribute(
                        "message",
                        "Tài khoản đã được kích hoạt trước đó."
                );
            }

            default -> {
                model.addAttribute(
                        "success",
                        false
                );
                model.addAttribute(
                        "message",
                        "Liên kết xác nhận không hợp lệ."
                );
            }
        }

        return "auth/verify-result";
    }
}
