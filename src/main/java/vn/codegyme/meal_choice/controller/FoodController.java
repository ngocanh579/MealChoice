package vn.codegyme.meal_choice.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import vn.codegyme.meal_choice.dto.food.FoodCreateRequest;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.dto.food.FoodUpdateRequest;
import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.FoodCategory;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.FoodService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final FoodRepository foodRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    // Merchant đang đăng nhập
    private Merchant getCurrentMerchant() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        UUID userId = userDetails.getId();

        return merchantRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));
    }

    // ==================== MERCHANT FOOD API ====================

    // Thêm món
    @PostMapping(
            value = "/api/merchant/foods",
            consumes = "multipart/form-data"
    )
    @ResponseBody
    public ResponseEntity<FoodResponse> createFood(
            @Valid @ModelAttribute FoodCreateRequest request,
            @RequestParam("images") List<MultipartFile> images) {

        Merchant merchant = getCurrentMerchant();

        FoodResponse response = foodService.createFood(
                merchant.getId(),
                request,
                images
        );

        return ResponseEntity.ok(response);
    }

    // Lấy danh sách món
    @GetMapping("/api/merchant/foods")
    @ResponseBody
    public ResponseEntity<List<FoodResponse>> getFoods() {

        Merchant merchant = getCurrentMerchant();

        return ResponseEntity.ok(
                foodService.getFoods(merchant.getId())
        );
    }

    // Chi tiết món
    @GetMapping("/api/merchant/foods/{foodId}")
    @ResponseBody
    public ResponseEntity<FoodResponse> getFood(
            @PathVariable Long foodId) {

        Merchant merchant = getCurrentMerchant();

        return ResponseEntity.ok(
                foodService.getFood(
                        merchant.getId(),
                        foodId
                )
        );
    }

    // Cập nhật món
    @PutMapping(
            value = "/api/merchant/foods/{foodId}",
            consumes = "multipart/form-data"
    )
    @ResponseBody
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long foodId,
            @Valid @ModelAttribute FoodUpdateRequest request,
            @RequestParam(
                    value = "images",
                    required = false
            ) List<MultipartFile> images) {

        Merchant merchant = getCurrentMerchant();

        return ResponseEntity.ok(
                foodService.updateFood(
                        merchant.getId(),
                        foodId,
                        request,
                        images
                )
        );
    }

    // Xóa mềm
    @DeleteMapping("/api/merchant/foods/{foodId}")
    @ResponseBody
    public ResponseEntity<String> deleteFood(
            @PathVariable Long foodId) {

        Merchant merchant = getCurrentMerchant();

        foodService.deleteFood(
                merchant.getId(),
                foodId
        );

        return ResponseEntity.ok("Xóa món ăn thành công");
    }

    // Bật/tắt đề xuất
    @PatchMapping("/api/merchant/foods/{foodId}/recommend")
    @ResponseBody
    public ResponseEntity<Void> toggleRecommendation(
            @PathVariable Long foodId) {

        Merchant merchant = getCurrentMerchant();

        foodService.toggleRecommendation(
                merchant.getId(),
                foodId
        );

        return ResponseEntity.ok().build();
    }

    // ==================== FOOD DETAIL PAGE ====================

    @Transactional
    @GetMapping("/foods/{id}")
    public String foodDetailPage(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        try {
            Food food = foodRepository.findById(id).orElse(null);

            if (food == null
                    || !Boolean.TRUE.equals(food.getIsActive())
                    || food.getDeletedAt() != null) {
                return "redirect:/";
            }

            model.addAttribute("food", food);

            List<String> foodImagesList = new ArrayList<>();

            if (food.getImages() != null) {
                food.getImages().stream()
                        .filter(image ->
                                image.getImageUrl() != null
                                        && !image.getImageUrl().isBlank())
                        .sorted((a, b) -> Boolean.compare(
                                !Boolean.TRUE.equals(a.getIsPrimary()),
                                !Boolean.TRUE.equals(b.getIsPrimary())
                        ))
                        .forEach(image -> {
                            if (!foodImagesList.contains(
                                    image.getImageUrl())) {
                                foodImagesList.add(
                                        image.getImageUrl()
                                );
                            }
                        });
            }

            model.addAttribute(
                    "foodImagesList",
                    foodImagesList
            );

            String city = getFoodCity(food);

            List<Food> foodsInCity;

            if (!city.isBlank()) {
                foodsInCity =
                        foodRepository.findAllActiveInCity(city);
            } else {
                foodsInCity =
                        foodRepository
                                .findAllByIsActiveTrueAndDeletedAtIsNullOrderByIdDesc(
                                        org.springframework.data.domain.Pageable.unpaged()
                                )
                                .getContent();
            }

            List<Food> recommendedFoods =
                    buildRecommendedFoods(
                            food,
                            foodsInCity
                    );

            model.addAttribute(
                    "recommendedFoods",
                    recommendedFoods
            );

            List<Food> peopleAlsoLiked =
                    buildPeopleAlsoLiked(
                            food,
                            foodsInCity
                    );

            model.addAttribute(
                    "peopleAlsoLiked",
                    peopleAlsoLiked
            );

            addUserInformation(
                    authentication,
                    model
            );

            return "food/detail";

        } catch (Exception e) {

            log.error(
                    "Lỗi tải chi tiết món: {}",
                    e.getMessage(),
                    e
            );

            return "redirect:/";
        }
    }

    // Lấy thành phố
    private String getFoodCity(Food food) {

        MerchantAddress address =
                food.getMerchantAddress();

        if (address == null) {
            return "";
        }

        String fullAddress =
                address.getMerchantAddress();

        if (fullAddress == null
                || fullAddress.isBlank()) {
            return "";
        }

        String[] parts =
                fullAddress.split(",");

        return parts[parts.length - 1].trim();
    }

    // Món đề xuất
    private List<Food> buildRecommendedFoods(
            Food food,
            List<Food> foodsInCity) {

        List<Food> recommendedFoods =
                new ArrayList<>();

        Merchant merchant =
                food.getMerchant();

        if (merchant == null) {
            return recommendedFoods;
        }

        Long currentFoodId =
                food.getId();

        List<FoodCategory> categories =
                food.getFoodCategories();

        List<Long> categoryIds =
                categories == null
                        ? Collections.emptyList()
                        : categories.stream()
                        .map(FoodCategory::getId)
                        .toList();

        List<Food> candidates =
                foodsInCity.stream()
                        .filter(f ->
                                f != null
                                        && !f.getId()
                                        .equals(currentFoodId))
                        .toList();

        List<Food> sameMerchantAndCategory =
                candidates.stream()
                        .filter(f ->
                                f.getMerchant() != null
                                        && f.getMerchant()
                                        .getId()
                                        .equals(merchant.getId()))
                        .filter(f ->
                                f.getFoodCategories() != null
                                        && f.getFoodCategories()
                                        .stream()
                                        .anyMatch(category ->
                                                categoryIds.contains(
                                                        category.getId())))
                        .sorted((a, b) ->
                                Long.compare(
                                        b.getId(),
                                        a.getId()))
                        .toList();

        recommendedFoods.addAll(
                sameMerchantAndCategory
        );

        if (recommendedFoods.size() < 8) {

            List<Food> sameMerchant =
                    candidates.stream()
                            .filter(f ->
                                    f.getMerchant() != null
                                            && f.getMerchant()
                                            .getId()
                                            .equals(merchant.getId()))
                            .filter(f ->
                                    !recommendedFoods.contains(f))
                            .sorted((a, b) ->
                                    Long.compare(
                                            b.getId(),
                                            a.getId()))
                            .toList();

            recommendedFoods.addAll(
                    sameMerchant
            );
        }

        return recommendedFoods.stream()
                .limit(8)
                .toList();
    }

    // Món cùng danh mục
    private List<Food> buildPeopleAlsoLiked(
            Food food,
            List<Food> foodsInCity) {

        if (food.getFoodCategories() == null
                || food.getFoodCategories().isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> categoryIds =
                food.getFoodCategories()
                        .stream()
                        .map(FoodCategory::getId)
                        .toList();

        return foodsInCity.stream()
                .filter(f ->
                        f != null
                                && !f.getId()
                                .equals(food.getId()))
                .filter(f ->
                        f.getFoodCategories() != null
                                && f.getFoodCategories()
                                .stream()
                                .anyMatch(category ->
                                        categoryIds.contains(
                                                category.getId())))
                .limit(8)
                .toList();
    }

    // ==================== USER ====================

    private void addUserInformation(
            Authentication authentication,
            Model model) {

        boolean isLoggedIn = false;
        String userDisplayName = null;
        boolean isAdmin = false;
        boolean isMerchant = false;

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal()
                instanceof CustomUserDetails userDetails) {

            Optional<User> userOpt =
                    userRepository.findByEmail(
                            userDetails.getUsername()
                    );

            if (userOpt.isPresent()) {

                User user = userOpt.get();

                isLoggedIn = true;
                userDisplayName =
                        user.getDisplayName();

                if (user.getRoles() != null) {

                    isAdmin =
                            user.getRoles()
                                    .stream()
                                    .anyMatch(role ->
                                            role.getName() != null
                                                    && role.getName()
                                                    .name()
                                                    .contains("ADMIN"));

                    isMerchant =
                            user.getRoles()
                                    .stream()
                                    .anyMatch(role ->
                                            role.getName() != null
                                                    && role.getName()
                                                    .name()
                                                    .contains("MERCHANT"));
                }
            }
        }

        model.addAttribute(
                "isLoggedIn",
                isLoggedIn
        );

        model.addAttribute(
                "userDisplayName",
                userDisplayName
        );

        model.addAttribute(
                "isAdmin",
                isAdmin
        );

        model.addAttribute(
                "isMerchant",
                isMerchant
        );
    }

    // ==================== LIKE FOOD ====================

    @ResponseBody
    @PostMapping("/api/foods/{id}/like")
    public ResponseEntity<?> likeFood(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Chưa đăng nhập");
        }

        // TODO: FoodLikeRepository

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/api/foods/{id}/unlike")
    public ResponseEntity<?> unlikeFood(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Chưa đăng nhập");
        }

        // TODO: FoodLikeRepository

        return ResponseEntity.ok().build();
    }

    // ==================== FOLLOW MERCHANT ====================

    @ResponseBody
    @PostMapping("/api/merchants/{id}/follow")
    public ResponseEntity<?> followMerchant(
            @PathVariable UUID id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Chưa đăng nhập");
        }

        // TODO: MerchantLikeRepository

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/api/merchants/{id}/unfollow")
    public ResponseEntity<?> unfollowMerchant(
            @PathVariable UUID id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Chưa đăng nhập");
        }

        // TODO: MerchantLikeRepository

        return ResponseEntity.ok().build();
    }
}