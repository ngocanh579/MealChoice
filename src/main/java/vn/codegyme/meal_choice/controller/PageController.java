package vn.codegyme.meal_choice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "auth/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/user/profile")
    public String profilePage() {
        return "user/profile";
    }

    @GetMapping("/user/address")
    public String addressPage() {
        return "user/address";
    }

    @GetMapping("/merchant/register")
    public String merchantRegisterPage() {
        return "merchant/register";
    }

    @GetMapping("/merchant/profile")
    public String merchantProfilePage() {
        return "merchant/profile";
    }
}
