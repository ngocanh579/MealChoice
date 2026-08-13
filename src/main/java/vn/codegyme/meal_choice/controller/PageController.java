package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.AuthService;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MerchantRepository merchantRepository;
    private final AuthService authService;

    @GetMapping({"/", "/home"})
    public String homePage() {
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/activate")
    public String activatePage(@RequestParam("token") String token, Model model) {
        try {
            authService.activateAccount(token);
            model.addAttribute("success", true);
            model.addAttribute("message", "Tài khoản của bạn đã được kích hoạt thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (RuntimeException ex) {
            model.addAttribute("success", false);
            model.addAttribute("message", ex.getMessage());
        }
        return "auth/activate";
    }

    @GetMapping("/user/profile")
    public String profilePage() {
        return "user/profile";
    }

    @GetMapping("/user/address")
    public String addressPage() {
        return "user/address";
    }

    @GetMapping("/merchants/register")
    public String merchantRegisterPage() {
        return "merchant/register";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/merchants/profile")
    public String merchantProfilePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String email = userDetails.getUsername();
            Optional<Merchant> merchantOpt = merchantRepository.findByMerchantEmailWithAddresses(email);
            if (merchantOpt.isPresent()) {
                model.addAttribute("merchant", merchantOpt.get());
                return "merchant/profile";
            }
        }

        // Fallback: Nếu không tìm thấy merchant theo email đăng nhập, lấy merchant đầu tiên
        List<Merchant> merchants = merchantRepository.findAllByOrderByIdDesc();
        if (!merchants.isEmpty()) {
            Optional<Merchant> mWithAddrs = merchantRepository.findByIdWithAddresses(merchants.get(0).getId());
            model.addAttribute("merchant", mWithAddrs.orElse(merchants.get(0)));
            return "merchant/profile";
        }

        return "redirect:/admin/merchants";
    }
}
