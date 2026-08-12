package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.AddressRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FoodController {

    private final FoodRepository foodRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // Helper lấy địa chỉ khu vực của user hiện tại
    private String getUserLocation(Authentication authentication) {
        try {
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
                if (userOpt.isPresent()) {
                    List<Address> addresses = addressRepository.findByUserId(userOpt.get().getId());
                    if (!addresses.isEmpty()) {
                        Address targetAddress = addresses.stream()
                                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                                .findFirst()
                                .orElse(addresses.get(addresses.size() - 1));
                        return targetAddress.getDistrict() != null ? targetAddress.getDistrict() : targetAddress.getCity();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể lấy user location: {}", e.getMessage());
        }
        return null;
    }

    // Helper nạp trạng thái thích món ăn và theo dõi quán ăn của user vào model
    private void addLikeStatusToModel(Authentication authentication, Model model) {
        boolean isLoggedIn = false;
        List<Long> likedFoodIds = Collections.emptyList();
        List<UUID> likedMerchantIds = Collections.emptyList();
        
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                isLoggedIn = true;
                UUID userId = userOpt.get().getId();
                likedFoodIds = foodRepository.findLikedFoodIdsByUserId(userId);
                likedMerchantIds = merchantRepository.findLikedMerchantIdsByUserId(userId);
            }
        }
        
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("likedFoodIds", likedFoodIds);
        model.addAttribute("likedMerchantIds", likedMerchantIds);
    }

    // Trang chi tiết món ăn (Food Detail Page) với các fragments
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/foods/{id}")
    public String foodDetailPage(@PathVariable("id") Long id,
                                Authentication authentication,
                                Model model) {
        try {
            addLikeStatusToModel(authentication, model);
            // 1. Tải món ăn chi tiết
            Optional<Food> foodOpt = foodRepository.findById(id);
            if (foodOpt.isEmpty() || !Boolean.TRUE.equals(foodOpt.get().getIsActive())) {
                return "redirect:/";
            }
            Food food = foodOpt.get();
            model.addAttribute("food", food);

            // Nạp danh sách tất cả các ảnh của món ăn (bao gồm cả ảnh chính ở vị trí 0)
            List<String> foodImagesList = new ArrayList<>();
            if (food.getImageUrl() != null && !food.getImageUrl().isBlank()) {
                foodImagesList.add(food.getImageUrl());
            }
            if (food.getFoodImages() != null) {
                for (vn.codegyme.meal_choice.entity.FoodImage img : food.getFoodImages()) {
                    if (img.getImageUrl() != null && !img.getImageUrl().isBlank() && !foodImagesList.contains(img.getImageUrl())) {
                        foodImagesList.add(img.getImageUrl());
                    }
                }
            }
            model.addAttribute("foodImagesList", foodImagesList);

            // 2. Trích xuất thành phố từ MerchantAddress của món ăn
            String city = "";
            if (food.getMerchant() != null && food.getMerchant().getAddresses() != null && !food.getMerchant().getAddresses().isEmpty()) {
                String fullAddress = food.getMerchant().getAddresses().get(0).getMerchantAddress();
                if (fullAddress != null && !fullAddress.isBlank()) {
                    String[] parts = fullAddress.split(",");
                    city = parts[parts.length - 1].trim();
                }
            }

            // 3. Tải danh sách món ăn cùng thành phố
            List<Food> foodsInCity = new ArrayList<>();
            if (!city.isBlank()) {
                foodsInCity = foodRepository.findAllActiveInCity(city);
            } else {
                foodsInCity = foodRepository.findAll();
            }

            // 4. Khối "Món dành riêng cho bạn" (Recommended Foods) - 8 món
            final List<Food> recommendedFoods = new ArrayList<>();
            if (food.getMerchant() != null && food.getCategory() != null) {
                UUID merchantId = food.getMerchant().getId();
                Long categoryId = food.getCategory().getId();
                
                // Danh sách món cùng thành phố (loại trừ món hiện tại)
                List<Food> cityFoodsExcludeCurrent = foodsInCity.stream()
                        .filter(f -> f != null && !f.getId().equals(id))
                        .toList();
                
                // Lấy phần 1: cùng category & cùng merchant, xếp theo lượt thích giảm dần
                List<Food> part1 = cityFoodsExcludeCurrent.stream()
                        .filter(f -> f.getMerchant() != null && f.getMerchant().getId().equals(merchantId)
                                && f.getCategory() != null && f.getCategory().getId().equals(categoryId))
                        .sorted((f1, f2) -> {
                            int likes1 = f1.getLikedByUsers() != null ? f1.getLikedByUsers().size() : 0;
                            int likes2 = f2.getLikedByUsers() != null ? f2.getLikedByUsers().size() : 0;
                            return Integer.compare(likes2, likes1);
                        })
                        .toList();
                recommendedFoods.addAll(part1);
                
                // Lấy phần 2: cùng category & cùng merchant, xếp theo ID giảm dần (loại bỏ món đã lấy)
                if (recommendedFoods.size() < 8) {
                    List<Food> part2 = cityFoodsExcludeCurrent.stream()
                           .filter(f -> f.getMerchant() != null && f.getMerchant().getId().equals(merchantId)
                                   && f.getCategory() != null && f.getCategory().getId().equals(categoryId))
                           .filter(f -> !recommendedFoods.contains(f))
                           .sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
                           .toList();
                    recommendedFoods.addAll(part2);
                }
                
                // Lấy phần 3: cùng merchant, xếp theo ID giảm dần (loại bỏ món đã lấy)
                if (recommendedFoods.size() < 8) {
                    List<Food> part3 = cityFoodsExcludeCurrent.stream()
                            .filter(f -> f.getMerchant() != null && f.getMerchant().getId().equals(merchantId))
                            .filter(f -> !recommendedFoods.contains(f))
                            .sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
                            .toList();
                    recommendedFoods.addAll(part3);
                }
            }
            
            List<Food> finalRecommended = recommendedFoods;
            if (recommendedFoods.size() > 8) {
                finalRecommended = recommendedFoods.subList(0, 8);
            }
            model.addAttribute("recommendedFoods", finalRecommended);

            // 5. Khối "Mọi người cũng thích" (People Also Liked) - 8 món
            List<Food> peopleAlsoLiked = new ArrayList<>();
            if (food.getCategory() != null) {
                Long categoryId = food.getCategory().getId();
                peopleAlsoLiked = foodsInCity.stream()
                        .filter(f -> f != null && !f.getId().equals(id) && f.getCategory() != null && f.getCategory().getId().equals(categoryId))
                        .sorted((f1, f2) -> {
                            int m1Likes = (f1.getMerchant() != null && f1.getMerchant().getLikedByUsers() != null) ? f1.getMerchant().getLikedByUsers().size() : 0;
                            int m2Likes = (f2.getMerchant() != null && f2.getMerchant().getLikedByUsers() != null) ? f2.getMerchant().getLikedByUsers().size() : 0;
                            return Integer.compare(m2Likes, m1Likes);
                        })
                        .limit(8)
                        .toList();
            }
            model.addAttribute("peopleAlsoLiked", peopleAlsoLiked);

            // 6. Truyền location của user
            model.addAttribute("userLocation", getUserLocation(authentication));

            // 7. Kiểm tra trạng thái đăng nhập
            boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());
            model.addAttribute("isLoggedIn", isLoggedIn);

        } catch (Exception e) {
            log.error("Lỗi khi tải trang chi tiết món ăn: {}", e.getMessage(), e);
            return "redirect:/";
        }
        return "food/detail";
    }

    // 8. REST API: Thích món ăn (Food Like)
    @ResponseBody
    @PostMapping("/api/foods/{id}/like")
    public org.springframework.http.ResponseEntity<?> likeFood(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        try {
            Optional<Food> foodOpt = foodRepository.findById(id);
            if (foodOpt.isEmpty()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            Food food = foodOpt.get();
            String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                food.getLikedByUsers().add(user);
                foodRepository.save(food);
                return org.springframework.http.ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi thích món ăn: {}", e.getMessage(), e);
        }
        return org.springframework.http.ResponseEntity.badRequest().build();
    }

    // 9. REST API: Hủy thích món ăn (Food Unlike)
    @ResponseBody
    @PostMapping("/api/foods/{id}/unlike")
    public org.springframework.http.ResponseEntity<?> unlikeFood(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        try {
            Optional<Food> foodOpt = foodRepository.findById(id);
            if (foodOpt.isEmpty()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            Food food = foodOpt.get();
            String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                food.getLikedByUsers().remove(user);
                foodRepository.save(food);
                return org.springframework.http.ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi hủy thích món ăn: {}", e.getMessage(), e);
        }
        return org.springframework.http.ResponseEntity.badRequest().build();
    }

    // 10. REST API: Theo dõi quán ăn (Merchant Follow)
    @ResponseBody
    @PostMapping("/api/merchants/{id}/follow")
    public org.springframework.http.ResponseEntity<?> followMerchant(@PathVariable("id") UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(id);
            if (merchantOpt.isEmpty()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            Merchant merchant = merchantOpt.get();
            String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                merchant.getLikedByUsers().add(user);
                merchantRepository.save(merchant);
                return org.springframework.http.ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi theo dõi quán: {}", e.getMessage(), e);
        }
        return org.springframework.http.ResponseEntity.badRequest().build();
    }

    // 11. REST API: Hủy theo dõi quán ăn (Merchant Unfollow)
    @ResponseBody
    @PostMapping("/api/merchants/{id}/unfollow")
    public org.springframework.http.ResponseEntity<?> unfollowMerchant(@PathVariable("id") UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(id);
            if (merchantOpt.isEmpty()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            Merchant merchant = merchantOpt.get();
            String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                merchant.getLikedByUsers().remove(user);
                merchantRepository.save(merchant);
                return org.springframework.http.ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi hủy theo dõi quán: {}", e.getMessage(), e);
        }
        return org.springframework.http.ResponseEntity.badRequest().build();
    }
}
