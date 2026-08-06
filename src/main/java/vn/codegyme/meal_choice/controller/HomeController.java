package vn.codegyme.meal_choice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/verify-success")
    public String verificationResult() {
        return "auth/verify-success";
    }

    @GetMapping("/merchant/register")
    public String merchantRegister() {
        return "merchant/register";
    }

    @GetMapping("/merchant/profile")
    public String merchantProfile() {
        return "merchant/profile";
    }

    @GetMapping("/admin/merchants")
    public String merchantList() {
        return "admin/merchant-list";
    }

    @GetMapping("/admin/merchants/{merchantId}")
    public String merchantDetail(@PathVariable UUID merchantId, Model model) {
        model.addAttribute("merchantId", merchantId);
        return "admin/merchant-detail";
    }
}
