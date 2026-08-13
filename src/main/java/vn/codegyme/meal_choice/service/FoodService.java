package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.food.FoodCreateRequest;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.FoodCategoryRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;

    // Thêm món ăn
    @Transactional
    public FoodResponse createFood(UUID merchantId, FoodCreateRequest request) {
        Merchant merchant = getApprovedMerchant(merchantId);

        MerchantAddress address = merchantAddressRepository.findById(request.getMerchantAddressId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ Merchant"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("Địa chỉ không thuộc Merchant này");
        }

        List<FoodCategory> categories = foodCategoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new RuntimeException("Một hoặc nhiều danh mục không tồn tại");
        }

        Food food = Food.builder()
                .merchant(merchant)
                .merchantAddress(address)
                .category(!categories.isEmpty() ? categories.get(0) : null)
                .name(request.getFoodName())
                .preparationTime(request.getPreparationTime())
                .note(request.getFoodNote())
                .price(request.getPrice() != null ? request.getPrice().doubleValue() : 0.0)
                .discountPrice(request.getDiscountPrice() != null ? request.getDiscountPrice().doubleValue() : null)
                .serviceFee(request.getServiceFee() != null ? request.getServiceFee().doubleValue() : 0.0)
                .isRecommended(false)
                .views(0)
                .orderCount(0)
                .build();

        return mapToResponse(foodRepository.save(food));
    }

    // Lấy danh sách món
    public List<FoodResponse> getFoods(UUID merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        return foodRepository.findByMerchant_IdAndDeletedAtIsNull(merchantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Lấy chi tiết món
    public FoodResponse getFood(UUID merchantId, Long foodId) {
        Food food = foodRepository.findByIdAndMerchant_IdAndDeletedAtIsNull(foodId, merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        return mapToResponse(food);
    }

    // Cập nhật món ăn
    @Transactional
    public FoodResponse updateFood(UUID merchantId, Long foodId, FoodCreateRequest request) {
        Merchant merchant = getApprovedMerchant(merchantId);

        Food food = foodRepository.findByIdAndMerchant_IdAndDeletedAtIsNull(foodId, merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        MerchantAddress address = merchantAddressRepository.findById(request.getMerchantAddressId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ Merchant"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("Địa chỉ không thuộc Merchant này");
        }

        List<FoodCategory> categories = foodCategoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new RuntimeException("Một hoặc nhiều danh mục không tồn tại");
        }

        food.setMerchant(merchant);
        food.setMerchantAddress(address);
        food.setFoodCategories(categories);
        food.setCategory(!categories.isEmpty() ? categories.get(0) : null);
        food.setFoodName(request.getFoodName());
        food.setPreparationTime(request.getPreparationTime());
        food.setFoodNote(request.getFoodNote());
        food.setPrice(request.getPrice() != null ? request.getPrice().doubleValue() : 0.0);
        food.setDiscountPrice(request.getDiscountPrice() != null ? request.getDiscountPrice().doubleValue() : null);
        food.setServiceFee(request.getServiceFee() != null ? request.getServiceFee().doubleValue() : 0.0);

        return mapToResponse(foodRepository.save(food));
    }

    // Xóa mềm món ăn
    @Transactional
    public void deleteFood(UUID merchantId, Long foodId) {
        Merchant merchant = getApprovedMerchant(merchantId);

        Food food = foodRepository.findByIdAndMerchant_IdAndDeletedAtIsNull(foodId, merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        food.setMerchant(merchant);
        food.setDeletedAt(LocalDateTime.now());
        foodRepository.save(food);
    }

    // Kiểm tra Merchant
    private Merchant getApprovedMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Merchant"));

        if (merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new RuntimeException("Merchant chưa được Admin phê duyệt");
        }

        return merchant;
    }

    // Map Entity sang Response DTO
    private FoodResponse mapToResponse(Food food) {
        FoodResponse response = new FoodResponse();

        response.setId(food.getId());

        if (food.getMerchantAddress() != null) {
            response.setMerchantAddressId(food.getMerchantAddress().getId());
            response.setMerchantAddress(food.getMerchantAddress().getMerchantAddress());
        }

        response.setFoodName(food.getFoodName());
        response.setPreparationTime(food.getPreparationTime());
        response.setFoodNote(food.getFoodNote());
        response.setPrice(food.getPriceAsBigDecimal());
        response.setDiscountPrice(food.getDiscountPriceAsBigDecimal());
        response.setServiceFee(food.getServiceFeeAsBigDecimal());
        response.setViews(food.getViews());
        response.setOrderCount(food.getOrderCount());
        response.setIsRecommended(food.getIsRecommended());

        if (food.getFoodCategories() != null) {
            response.setCategoryIds(
                    food.getFoodCategories()
                            .stream()
                            .map(FoodCategory::getId)
                            .toList()
            );
        }

        if (food.getImages() != null) {
            response.setImageUrls(
                    food.getImages()
                            .stream()
                            .map(FoodImage::getImageUrl)
                            .toList()
            );
        }

        response.setCreatedAt(food.getCreatedAt());
        response.setUpdatedAt(food.getUpdatedAt());

        return response;
    }
}