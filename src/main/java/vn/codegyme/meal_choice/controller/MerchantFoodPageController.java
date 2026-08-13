package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.FoodService;

@Controller
@RequestMapping("/merchant/foods")
@RequiredArgsConstructor
public class MerchantFoodPageController {

    private final FoodService foodService;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    @GetMapping
    public String showFoodList(
            @RequestParam(required = false) String foodName,
            Authentication authentication,
            Model model) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        Merchant merchant = merchantRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa đăng ký Merchant"));

        model.addAttribute("merchant", merchant);
        model.addAttribute("foodName", foodName == null ? "" : foodName);

        if (foodName == null || foodName.trim().isEmpty()) {
            model.addAttribute("foods", foodService.getFoods(merchant.getId()));
        } else {
            model.addAttribute("foods", foodService.searchFoods(merchant.getId(), foodName));
        }

        return "food/list";


    }
}
