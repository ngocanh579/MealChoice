package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegyme.meal_choice.service.AccountActivationService;

@Controller
@RequiredArgsConstructor
public class AccountVerificationController {

    private final AccountActivationService accountActivationService;

    @GetMapping("/api/account/verify")
    public String verify(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            String email = accountActivationService.activate(token);
            redirectAttributes.addAttribute("status", "success");
            redirectAttributes.addAttribute("email", email);
        } catch (RuntimeException exception) {
            redirectAttributes.addAttribute("status", "error");
            redirectAttributes.addAttribute("message", exception.getMessage());
        }
        return "redirect:/verify-success";
    }
}
