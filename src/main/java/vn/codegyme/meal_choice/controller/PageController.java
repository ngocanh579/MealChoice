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
import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.entity.Category;
import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.AddressRepository;
import vn.codegyme.meal_choice.repository.CategoryRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {

    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;
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

    // 1. TRANG CHỦ
    @GetMapping({"/", "/home"})
    public String homePage(@RequestParam(name = "page", defaultValue = "0") int page,
                           Authentication authentication,
                           Model model) {
        try {
            // 1. Danh sách Category
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories != null ? categories : Collections.emptyList());

            // Lấy danh sách thức ăn active để xử lý
            Page<Food> activeFoodsPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(PageRequest.of(0, 100));
            List<Food> allActive = activeFoodsPage != null ? activeFoodsPage.getContent() : Collections.emptyList();

            // 2. 8 món ăn giảm giá nhiều nhất (Tính toán an toàn bằng Java Stream)
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

            // 3. 8 món ăn gợi ý theo vị trí
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

            // 4. Danh sách toàn bộ món ăn bên dưới (Phân trang 30 món/trang)
            Pageable pageable = PageRequest.of(Math.max(0, page), 30);
            Page<Food> foodPage = foodRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
            model.addAttribute("foodPage", foodPage);

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
                             Model model) {
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
        model.addAttribute("categories", categoryRepository.findAll());
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
        model.addAttribute("categories", categoryRepository.findAll());
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
                              Model model) {
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
        model.addAttribute("categories", categoryRepository.findAll());
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
                                   Model model) {
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
        model.addAttribute("categories", categoryRepository.findAll());
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
                                     Model model) {
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
        model.addAttribute("categories", categoryRepository.findAll());
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

    @GetMapping("/user/profile")
    public String profilePage() {
        return "user/profile";
    }

    @GetMapping("/user/address")
    public String addressPage() {
        return "redirect:/user/profile";
    }

    @GetMapping("/merchant/register")
    public String merchantRegisterPage() {
        return "merchant/register";
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

        List<Merchant> merchants = merchantRepository.findAllByOrderByIdDesc();
        if (!merchants.isEmpty()) {
            Optional<Merchant> mWithAddrs = merchantRepository.findByIdWithAddresses(merchants.get(0).getId());
            model.addAttribute("merchant", mWithAddrs.orElse(merchants.get(0)));
            return "merchant/profile";
        }

        return "redirect:/admin/merchants";
    }
}
