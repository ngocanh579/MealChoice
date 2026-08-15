package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.FoodService;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MerchantRepository merchantRepository;
    private final FoodService foodService;

    @GetMapping("/")
    public String homePage() {
        return "redirect:/login?tab=register";
    }

    @GetMapping("/home")
    public String homePageAfterLogin() {
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/merchant/dashboard")
    public String merchantDashboardPage(Authentication authentication, Model model) {

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String email = userDetails.getUsername();
            Optional<Merchant> merchantOpt = merchantRepository.findByMerchantEmailWithAddresses(email);
            if (merchantOpt.isPresent()) {
                Merchant merchant = merchantOpt.get();
                model.addAttribute("merchant", merchant);
                model.addAttribute("foodCount", foodService.getFoods(merchant.getId()).size());
                return "merchant/dashboard";
            }
        }

        List<Merchant> merchants = merchantRepository.findAllByOrderByIdDesc();
        if (!merchants.isEmpty()) {
            Optional<Merchant> mWithAddrs = merchantRepository.findByIdWithAddresses(merchants.get(0).getId());
            Merchant merchant = mWithAddrs.orElse(merchants.get(0));
            model.addAttribute("merchant", merchant);
            model.addAttribute("foodCount", foodService.getFoods(merchant.getId()).size());
            return "merchant/dashboard";
        }

        return "merchant/dashboard";
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/merchant/profile")
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
