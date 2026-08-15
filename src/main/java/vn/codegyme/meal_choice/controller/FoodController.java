package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.codegyme.meal_choice.dto.food.FoodCreateRequest;
import vn.codegyme.meal_choice.dto.food.FoodUpdateRequest;;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.FoodService;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchant/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final MerchantRepository merchantRepository;

    // Lấy Merchant của tài khoản đang đăng nhập
    private Merchant getCurrentMerchant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        return merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Merchant"));
    }

    // Thêm món ăn
    @PostMapping(consumes = "multipart/form-data")
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

    // Lấy danh sách món ăn
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getFoods() {
        Merchant merchant = getCurrentMerchant();

        return ResponseEntity.ok(
                foodService.getFoods(merchant.getId())
        );
    }

    // Lấy chi tiết món ăn
    @GetMapping("/{foodId}")
    public ResponseEntity<FoodResponse> getFood(
            @PathVariable UUID foodId) {

        Merchant merchant = getCurrentMerchant();

        return ResponseEntity.ok(
                foodService.getFood(
                        merchant.getId(),
                        foodId
                )
        );
    }

    // Cập nhật món ăn
    @PutMapping(value = "/{foodId}", consumes = "multipart/form-data")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable UUID foodId,
            @Valid @ModelAttribute FoodUpdateRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

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

    // Xóa mềm món ăn
    @DeleteMapping("/{foodId}")
    public ResponseEntity<String> deleteFood(
            @PathVariable UUID foodId) {

        Merchant merchant = getCurrentMerchant();

        foodService.deleteFood(
                merchant.getId(),
                foodId
        );

        return ResponseEntity.ok("Xóa món ăn thành công");
    }

    @PatchMapping("/{foodId}/recommend")
    public ResponseEntity<Void> toggleRecommendation(
            @PathVariable UUID foodId) {

        Merchant merchant = getCurrentMerchant();

        foodService.toggleRecommendation(
                merchant.getId(),
                foodId
        );

        return ResponseEntity.ok().build();
    }
}