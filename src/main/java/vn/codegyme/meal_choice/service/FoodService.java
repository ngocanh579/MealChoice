package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.food.FoodCreateRequest;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.FoodCategory;
import vn.codegyme.meal_choice.entity.FoodImage;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.repository.FoodCategoryRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;

import java.math.BigDecimal;
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
                .foodCategories(categories)
                .foodName(request.getFoodName())
                .preparationTime(request.getPreparationTime())
                .foodNote(request.getFoodNote())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .serviceFee(request.getServiceFee() != null ? request.getServiceFee() : BigDecimal.ZERO)
                .isRecommended(false)
                .views(0)
                .orderCount(0)
                .build();

        return mapToResponse(foodRepository.save(food));
    }

    public List<FoodResponse> getFoods(UUID merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        return foodRepository.findByMerchant_IdAndDeletedAtIsNullOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FoodResponse> searchFoods(UUID merchantId, String foodName) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        if (foodName == null || foodName.trim().isEmpty()) {
            return getFoods(merchantId);
        }

        return foodRepository
                .findByMerchant_IdAndFoodNameContainingIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(
                        merchantId,
                        foodName.trim()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FoodResponse getFood(UUID merchantId, UUID foodId) {
        Food food = foodRepository.findByIdAndMerchant_IdAndDeletedAtIsNull(foodId, merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        return mapToResponse(food);
    }

    @Transactional
    public FoodResponse updateFood(UUID merchantId, UUID foodId, FoodCreateRequest request) {
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
        food.setFoodName(request.getFoodName());
        food.setPreparationTime(request.getPreparationTime());
        food.setFoodNote(request.getFoodNote());
        food.setPrice(request.getPrice());
        food.setDiscountPrice(request.getDiscountPrice());
        food.setServiceFee(request.getServiceFee() != null ? request.getServiceFee() : BigDecimal.ZERO);

        return mapToResponse(foodRepository.save(food));
    }

    @Transactional
    public void deleteFood(UUID merchantId, UUID foodId) {
        Merchant merchant = getApprovedMerchant(merchantId);

        Food food = foodRepository.findByIdAndMerchant_IdAndDeletedAtIsNull(foodId, merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        food.setMerchant(merchant);
        food.setDeletedAt(LocalDateTime.now());

        foodRepository.save(food);
    }

    private Merchant getApprovedMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Merchant"));

        if (merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new RuntimeException("Merchant chưa được Admin phê duyệt");
        }

        return merchant;
    }

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
        response.setPrice(food.getPrice());
        response.setDiscountPrice(food.getDiscountPrice());
        response.setServiceFee(food.getServiceFee());
        response.setViews(food.getViews());
        response.setOrderCount(food.getOrderCount());
        response.setIsRecommended(food.getIsRecommended());

        response.setCategoryIds(
                food.getFoodCategories()
                        .stream()
                        .map(FoodCategory::getId)
                        .toList()
        );

        response.setImageUrls(
                food.getImages()
                        .stream()
                        .map(FoodImage::getImageUrl)
                        .toList()
        );

        response.setCreatedAt(food.getCreatedAt());
        response.setUpdatedAt(food.getUpdatedAt());

        return response;
    }
}
