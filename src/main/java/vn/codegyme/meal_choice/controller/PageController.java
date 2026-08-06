package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MerchantRepository merchantRepository;

    @GetMapping({"/", "/home"})
    public String homePage() {
        return "home";
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
    public String merchantProfilePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String email = userDetails.getUsername();
            Optional<Merchant> merchantOpt = merchantRepository.findByMerchantEmail(email);
            if (merchantOpt.isPresent()) {
                return "redirect:/admin/merchants/" + merchantOpt.get().getId();
            }
        }

        // Fallback: Nếu không tìm thấy merchant theo email đăng nhập, lấy merchant đầu tiên
        List<Merchant> merchants = merchantRepository.findAllByOrderByIdDesc();
        if (!merchants.isEmpty()) {
            return "redirect:/admin/merchants/" + merchants.get(0).getId();
        }

        return "redirect:/admin/merchants";
    }
}
