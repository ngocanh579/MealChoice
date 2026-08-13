package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.*;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.AuthService;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {

    private final MerchantRepository merchantRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AuthService authService;

    // Helper nạp ảnh đại diện cho từng Category (Ảnh của món ăn mới nhất thuộc Category)
    private List<FoodCategory> getCategoriesWithLatestFoodImage() {
        List<FoodCategory> categories = foodCategoryRepository.findAll();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy danh sách món ăn active gần nhất
        Page<Food> activePage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(PageRequest.of(0, 500));
        List<Food> allActiveFoods = activePage != null ? activePage.getContent() : Collections.emptyList();

        // Nhóm món ăn mới nhất (ID lớn nhất) theo Category ID
        Map<Long, Food> latestFoodByCategory = new HashMap<>();
        for (Food food : allActiveFoods) {
            if (food.getCategory() != null && food.getCategory().getId() != null) {
                latestFoodByCategory.putIfAbsent(food.getCategory().getId(), food);
            }
        }

        for (FoodCategory cat : categories) {
            Food latestFood = latestFoodByCategory.get(cat.getId());
            if (latestFood == null) {
                Optional<Food> foodOpt = foodRepository.findLatestFoodByCategoryId(cat.getId());
                if (foodOpt.isPresent()) {
                    latestFood = foodOpt.get();
                }
            }

            if (latestFood != null) {
                cat.setImageUrl(latestFood.getImageUrl());
            } else {
                cat.setImageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=150&auto=format&fit=crop&q=80");
            }
        }

        return categories;
    }

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

    // Helper nạp trạng thái thích món ăn, theo dõi quán ăn và thông tin tài khoản của user vào model
    private void addLikeStatusToModel(Authentication authentication, Model model) {
        boolean isLoggedIn = false;
        String userDisplayName = null;
        boolean isAdmin = false;
        boolean isMerchant = false;
        List<Long> likedFoodIds = Collections.emptyList();
        List<UUID> likedMerchantIds = Collections.emptyList();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                isLoggedIn = true;
                User user = userOpt.get();
                userDisplayName = user.getDisplayName();
                isAdmin = user.getRoles() != null && user.getRoles().stream()
                        .anyMatch(r -> r.getName() != null && r.getName().name().contains("ADMIN"));
                isMerchant = user.getRoles() != null && user.getRoles().stream()
                        .anyMatch(r -> r.getName() != null && r.getName().name().contains("MERCHANT"));
                UUID userId = user.getId();
                likedFoodIds = foodRepository.findLikedFoodIdsByUserId(userId);
                likedMerchantIds = merchantRepository.findLikedMerchantIdsByUserId(userId);
            }
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("userDisplayName", userDisplayName);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isMerchant", isMerchant);
        model.addAttribute("likedFoodIds", likedFoodIds);
        model.addAttribute("likedMerchantIds", likedMerchantIds);
    }

    // 1. TRANG CHỦ
    @GetMapping({"/", "/home"})
    public String homePage(@RequestParam(name = "page", defaultValue = "0") int page,
                           Authentication authentication,
                           Model model) {
        try {
            addLikeStatusToModel(authentication, model);

            // 1. Danh sách toàn bộ món ăn: "Gợi ý món ăn hôm nay" (Phân trang 30 món/trang)
            Pageable pageable = PageRequest.of(Math.max(0, page), 30);
            Page<Food> foodPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
            model.addAttribute("foodPage", foodPage);

            // 2. Danh sách Category (cho thanh tìm kiếm và slider)
            List<FoodCategory> categories = getCategoriesWithLatestFoodImage();
            model.addAttribute("categories", categories != null ? categories : Collections.emptyList());

            // 3. Chỉ tải "Gợi ý món ngon gần bạn" và "8 Món Ăn Giảm Giá Nhiều Nhất" ở trang đầu tiên (page == 0)
            if (page == 0) {
                Page<Food> activeFoodsPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(PageRequest.of(0, 100));
                List<Food> allActive = activeFoodsPage != null ? activeFoodsPage.getContent() : Collections.emptyList();

                // 8 món ăn giảm giá nhiều nhất
                List<Food> topDiscountFoods = allActive.stream()
                        .filter(f -> f != null && f.getDiscountPrice() != null && f.getPrice() != null && f.getPrice() > 0 && f.getDiscountPrice() < f.getPrice())
                        .sorted((f1, f2) -> {
                            double d1 = (f1.getPrice() - f1.getDiscountPrice()) / f1.getPrice();
                            double d2 = (f2.getPrice() - f2.getDiscountPrice()) / f2.getPrice();
                            return Double.compare(d2, d1);
                        })
                        .limit(8)
                        .toList();

                if (topDiscountFoods.size() < 8 && !allActive.isEmpty()) {
                    topDiscountFoods = allActive.stream().limit(8).toList();
                }
                model.addAttribute("topDiscountFoods", topDiscountFoods);

                // 8 món ăn gợi ý theo vị trí gần bạn
                List<Food> suggestedFoods = new ArrayList<>();
                String userLocation = getUserLocation(authentication);

                if (userLocation != null && !userLocation.isBlank()) {
                    String cleanLoc = userLocation.toLowerCase();
                    suggestedFoods = allActive.stream()
                            .filter(f -> f != null && f.getAddress() != null && f.getAddress().toLowerCase().contains(cleanLoc))
                            .limit(8)
                            .toList();
                }

                if (suggestedFoods.size() < 8 && !allActive.isEmpty()) {
                    suggestedFoods = allActive.stream().limit(8).toList();
                }
                model.addAttribute("suggestedFoods", suggestedFoods);
            } else {
                model.addAttribute("topDiscountFoods", Collections.emptyList());
                model.addAttribute("suggestedFoods", Collections.emptyList());
            }

        } catch (Exception e) {
            log.error("Lỗi khi load trang chủ: {}", e.getMessage(), e);
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("topDiscountFoods", Collections.emptyList());
            model.addAttribute("suggestedFoods", Collections.emptyList());
            model.addAttribute("foodPage", Page.empty());
        }

        return "home";
    }

    // 2. TRANG TÌM KIẾM & LỌC CATEGORY
    @GetMapping("/search")
    public String searchPage(@RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "categoryId", required = false) Long categoryId,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             Authentication authentication,
                             Model model) {
        addLikeStatusToModel(authentication, model);
        Pageable pageable = PageRequest.of(Math.max(0, page), 50);
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Food> foodPage;
        try {
            if (cleanKeyword != null && categoryId != null) {
                foodPage = foodRepository.searchByKeywordAndCategory(cleanKeyword, categoryId, pageable);
            } else if (cleanKeyword != null) {
                foodPage = foodRepository.searchByKeyword(cleanKeyword, pageable);
            } else if (categoryId != null) {
                foodPage = foodRepository.findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(categoryId, pageable);
            } else {
                foodPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
            }
        } catch (Exception e) {
            log.error("Lỗi tìm kiếm: {}", e.getMessage(), e);
            foodPage = Page.empty();
        }

        model.addAttribute("foodPage", foodPage);
        model.addAttribute("categories", getCategoriesWithLatestFoodImage());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUrl", "/search");
        model.addAttribute("pageTitle", "Kết Quả Tìm Kiếm");
        model.addAttribute("pageDescription", (cleanKeyword != null) ? "Kết quả tìm kiếm cho: \"" + cleanKeyword + "\"" : "Tất cả món ăn");

        return "homepage/food-list-page";
    }

    // 3. TRANG GẦN BẠN
    @GetMapping("/restaurants/near-you")
    public String nearYouPage(@RequestParam(name = "categoryId", required = false) Long categoryId,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              Authentication authentication,
                              Model model) {
        addLikeStatusToModel(authentication, model);
        Pageable pageable = PageRequest.of(Math.max(0, page), 50);
        String userLocation = getUserLocation(authentication);

        Page<Food> foodPage;
        try {
            if (userLocation != null && !userLocation.isBlank()) {
                foodPage = (categoryId != null)
                        ? foodRepository.findByLocationNearUserAndCategory(userLocation, categoryId, pageable)
                        : foodRepository.findByLocationNearUser(userLocation, pageable);

                if (foodPage.isEmpty()) {
                    foodPage = (categoryId != null)
                            ? foodRepository.findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(categoryId, pageable)
                            : foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
                }
            } else {
                foodPage = (categoryId != null)
                        ? foodRepository.findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(categoryId, pageable)
                        : foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
            }
        } catch (Exception e) {
            log.error("Lỗi trang gần bạn: {}", e.getMessage(), e);
            foodPage = Page.empty();
        }

        model.addAttribute("foodPage", foodPage);
        model.addAttribute("categories", getCategoriesWithLatestFoodImage());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUrl", "/restaurants/near-you");
        model.addAttribute("pageTitle", "Món Ngon Gần Bạn");
        model.addAttribute("pageDescription", userLocation != null ? "Các quán ăn gần khu vực " + userLocation : "Giao hàng siêu tốc từ các quán ăn gần bạn nhất");

        return "homepage/food-list-page";
    }

    // 4. TRANG NỔI BẬT / TOP PICKS
    @GetMapping({"/restaurants/popular", "/restaurants/top-picks"})
    public String popularPage(@RequestParam(name = "categoryId", required = false) Long categoryId,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              Authentication authentication,
                              Model model) {
        addLikeStatusToModel(authentication, model);
        Pageable pageable = PageRequest.of(Math.max(0, page), 50);
        Page<Food> foodPage;
        try {
            if (categoryId != null) {
                foodPage = foodRepository.findTopDiscountsByCategory(categoryId, pageable);
                if (foodPage.isEmpty()) {
                    foodPage = foodRepository.findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(categoryId, pageable);
                }
            } else {
                foodPage = foodRepository.findAllTopDiscounts(pageable);
                if (foodPage.isEmpty()) {
                    foodPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi trang nổi bật: {}", e.getMessage(), e);
            foodPage = Page.empty();
        }

        model.addAttribute("foodPage", foodPage);
        model.addAttribute("categories", getCategoriesWithLatestFoodImage());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUrl", "/restaurants/popular");
        model.addAttribute("pageTitle", "Món Ăn Nổi Bật & Bán Chạy");
        model.addAttribute("pageDescription", "Những món ăn được đánh giá cao nhất và đặt nhiều nhất");

        return "homepage/food-list-page";
    }

    // 5. TRANG SIÊU ƯU ĐÃI
    @GetMapping("/restaurants/top-discounts")
    public String topDiscountsPage(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   Authentication authentication,
                                   Model model) {
        addLikeStatusToModel(authentication, model);
        Pageable pageable = PageRequest.of(Math.max(0, page), 50);
        Page<Food> foodPage;
        try {
            if (categoryId != null) {
                foodPage = foodRepository.findTopDiscountsByCategory(categoryId, pageable);
            } else {
                foodPage = foodRepository.findAllTopDiscounts(pageable);
            }
        } catch (Exception e) {
            log.error("Lỗi trang ưu đãi: {}", e.getMessage(), e);
            foodPage = Page.empty();
        }

        model.addAttribute("foodPage", foodPage);
        model.addAttribute("categories", getCategoriesWithLatestFoodImage());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUrl", "/restaurants/top-discounts");
        model.addAttribute("pageTitle", "Siêu Ưu Đãi Giảm Giá Lên Đến 50%");
        model.addAttribute("pageDescription", "Tổng hợp các món ăn đang có ưu đãi giảm giá sâu nhất");

        return "homepage/food-list-page";
    }

    // 6. TRANG NHÀ HÀNG MỚI
    @GetMapping("/restaurants/new")
    public String newRestaurantsPage(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     Authentication authentication,
                                     Model model) {
        addLikeStatusToModel(authentication, model);
        Pageable pageable = PageRequest.of(Math.max(0, page), 50);
        Page<Food> foodPage;
        try {
            foodPage = (categoryId != null)
                    ? foodRepository.findAllByCategoryIdAndIsActiveTrueOrderByIdDesc(categoryId, pageable)
                    : foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
        } catch (Exception e) {
            log.error("Lỗi trang nhà hàng mới: {}", e.getMessage(), e);
            foodPage = Page.empty();
        }

        model.addAttribute("foodPage", foodPage);
        model.addAttribute("categories", getCategoriesWithLatestFoodImage());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUrl", "/restaurants/new");
        model.addAttribute("pageTitle", "Quán Ăn & Nhà Hàng Mới");
        model.addAttribute("pageDescription", "Khám phá những quán ăn và hương vị mới xuất hiện");

        return "homepage/food-list-page";
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
        return "redirect:/user/profile";
    }

    @GetMapping({"/merchants/register", "/merchant/register"})
    public String merchantRegisterPage() {
        return "merchant/register";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping({"/merchants/profile", "/merchant/profile"})
    public String merchantProfilePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String email = userDetails.getUsername();
            Optional<Merchant> merchantOpt = merchantRepository.findByMerchantEmailWithAddresses(email);
            if (merchantOpt.isPresent()) {
                model.addAttribute("merchant", merchantOpt.get());
                return "merchant/profile";
            }
        }

        List<Merchant> merchants = merchantRepository.findAllByOrderByIdDesc();
        if (!merchants.isEmpty()) {
            Optional<Merchant> mWithAddrs = merchantRepository.findByIdWithAddresses(merchants.get(0).getId());
            model.addAttribute("merchant", mWithAddrs.orElse(merchants.get(0)));
            return "merchant/profile";
        }

        return "redirect:/admin/merchants";
    }
}
