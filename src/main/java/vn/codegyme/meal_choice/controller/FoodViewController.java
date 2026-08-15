package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.FoodCategoryRepository;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.FoodService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/merchant/foods")
@RequiredArgsConstructor
public class FoodViewController {

    private final FoodService foodService;
    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    private Merchant getCurrentMerchant() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        UUID userId = userDetails.getId();

        return merchantRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));
    }

    // Danh sách món ăn
    // GET /merchant/foods
    @GetMapping
    public String list(Model model) {

        Merchant merchant = getCurrentMerchant();

        List<FoodResponse> foods =
                foodService.getFoods(merchant.getId());

        model.addAttribute("foods", foods);

        return "food/list";
    }

    // Form thêm món
    // GET /merchant/foods/create
    @GetMapping("/create")
    public String create(Model model) {

        Merchant merchant = getCurrentMerchant();

        // Địa chỉ của Merchant hiện tại
        model.addAttribute(
                "addresses",
                merchantAddressRepository.findByMerchantId(
                        merchant.getId()
                )
        );

        // Danh sách category
        model.addAttribute(
                "categories",
                foodCategoryRepository.findAll()
        );

        return "food/create";
    }

    // Chi tiết món
    // GET /merchant/foods/{foodId}
    @GetMapping("/{foodId}")
    public String detail(
            @PathVariable UUID foodId,
            Model model) {

        Merchant merchant = getCurrentMerchant();

        FoodResponse food =
                foodService.getFood(
                        merchant.getId(),
                        foodId
                );

        model.addAttribute("food", food);

        return "food/detail";
    }

    // Form chỉnh sửa món
    @GetMapping("/{foodId}/edit")
    public String edit(
            @PathVariable UUID foodId,
            Model model) {

        Merchant merchant = getCurrentMerchant();

        FoodResponse food =
                foodService.getFood(
                        merchant.getId(),
                        foodId
                );

        model.addAttribute("food", food);
        model.addAttribute(
                "addresses",
                merchantAddressRepository.findByMerchantId(
                        merchant.getId()
                )
        );
        model.addAttribute(
                "categories",
                foodCategoryRepository.findAll()
        );

        return "food/edit";
    }

}
