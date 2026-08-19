package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.codegyme.meal_choice.dto.food.FoodCreateRequest;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.dto.food.FoodUpdateRequest;
import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.FoodCategory;
import vn.codegyme.meal_choice.entity.FoodImage;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.Tag;
import vn.codegyme.meal_choice.repository.FoodCategoryRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.TagRepository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final TagRepository tagRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final FileStorageService fileStorageService;

    // Thêm món ăn
    @Transactional
    public FoodResponse createFood(
            UUID merchantId,
            FoodCreateRequest request,
            List<MultipartFile> images) {

        Merchant merchant = getApprovedMerchant(merchantId);

        if (images == null || images.size() < 2) {
            throw new RuntimeException("Món ăn phải có ít nhất 2 ảnh");
        }

        MerchantAddress address = merchantAddressRepository
                .findById(request.getMerchantAddressId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy địa chỉ Merchant"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("Địa chỉ không thuộc Merchant này");
        }

        List<FoodCategory> categories =
                foodCategoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new RuntimeException(
                    "Một hoặc nhiều danh mục không tồn tại");
        }

        List<Tag> tags = Collections.emptyList();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = tagRepository.findAllById(request.getTagIds());

            if (tags.size() != request.getTagIds().size()) {
                throw new RuntimeException(
                        "Một hoặc nhiều Tag không tồn tại");
            }
        }

        Food food = Food.builder()
                .merchant(merchant)
                .merchantAddress(address)
                .foodCategories(new ArrayList<>(categories))
                .tags(new ArrayList<>(tags))
                .foodName(request.getFoodName())
                .preparationTime(request.getPreparationTime())
                .foodNote(request.getFoodNote())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .serviceFee(
                        request.getServiceFee() != null
                                ? request.getServiceFee()
                                : BigDecimal.ZERO
                )
                .isActive(true)
                .isRecommended(false)
                .views(0)
                .orderCount(0)
                .build();

        food = foodRepository.save(food);

        if (food.getImages() == null) {
            food.setImages(new ArrayList<>());
        }

        for (int i = 0; i < images.size(); i++) {

            MultipartFile image = images.get(i);

            if (image == null || image.isEmpty()) {
                continue;
            }

            String imageUrl = fileStorageService.saveFoodImage(
                    food.getId(),
                    image
            );

            FoodImage foodImage = FoodImage.builder()
                    .food(food)
                    .imageUrl(imageUrl)
                    .isPrimary(i == 0)
                    .build();

            food.getImages().add(foodImage);
        }

        if (food.getImages().isEmpty()) {
            throw new RuntimeException("Món ăn phải có ít nhất 1 ảnh hợp lệ");
        }

        foodRepository.save(food);

        return mapToResponse(food);
    }

    // Lấy danh sách món ăn của Merchant
    @Transactional(readOnly = true)
    public List<FoodResponse> getFoods(UUID merchantId) {

        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        return foodRepository
                .findByMerchant_IdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        merchantId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Lấy chi tiết món ăn
    @Transactional(readOnly = true)
    public FoodResponse getFood(UUID merchantId, Long foodId) {

        Food food = foodRepository
                .findByIdAndMerchant_IdAndDeletedAtIsNull(
                        foodId,
                        merchantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món ăn"));

        return mapToResponse(food);
    }

    // Cập nhật món ăn
    @Transactional
    public FoodResponse updateFood(
            UUID merchantId,
            Long foodId,
            FoodUpdateRequest request,
            List<MultipartFile> images) {

        Merchant merchant = getApprovedMerchant(merchantId);

        Food food = foodRepository
                .findByIdAndMerchant_IdAndDeletedAtIsNull(
                        foodId,
                        merchantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món ăn"));

        MerchantAddress address = merchantAddressRepository
                .findById(request.getMerchantAddressId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy địa chỉ Merchant"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("Địa chỉ không thuộc Merchant này");
        }

        List<FoodCategory> categories =
                foodCategoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new RuntimeException(
                    "Một hoặc nhiều danh mục không tồn tại");
        }

        List<Tag> tags = Collections.emptyList();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = tagRepository.findAllById(request.getTagIds());

            if (tags.size() != request.getTagIds().size()) {
                throw new RuntimeException(
                        "Một hoặc nhiều Tag không tồn tại");
            }
        }

        food.setMerchant(merchant);
        food.setMerchantAddress(address);
        food.setFoodCategories(new ArrayList<>(categories));
        food.setTags(new ArrayList<>(tags));

        food.setFoodName(request.getFoodName());
        food.setPreparationTime(request.getPreparationTime());
        food.setFoodNote(request.getFoodNote());
        food.setPrice(request.getPrice());
        food.setDiscountPrice(request.getDiscountPrice());
        food.setServiceFee(
                request.getServiceFee() != null
                        ? request.getServiceFee()
                        : BigDecimal.ZERO
        );

        // Nếu có ảnh mới thì thêm vào món ăn
        if (images != null && !images.isEmpty()) {

            for (int i = images.size() - 1; i >= 0; i--) {

                MultipartFile image = images.get(i);

                if (image == null || image.isEmpty()) {
                    continue;
                }

                String imageUrl = fileStorageService.saveFoodImage(
                        food.getId(),
                        image
                );

                FoodImage foodImage = FoodImage.builder()
                        .food(food)
                        .imageUrl(imageUrl)
                        .isPrimary(i == images.size() - 1)
                        .build();

                food.getImages().add(foodImage);
            }
        }

        return mapToResponse(foodRepository.save(food));
    }

    // Xóa mềm món ăn
    @Transactional
    public void deleteFood(UUID merchantId, Long foodId) {

        Merchant merchant = getApprovedMerchant(merchantId);

        Food food = foodRepository
                .findByIdAndMerchant_IdAndDeletedAtIsNull(
                        foodId,
                        merchantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món ăn"));

        food.setMerchant(merchant);
        food.setDeletedAt(LocalDateTime.now());

        foodRepository.save(food);
    }

    // Kiểm tra Merchant
    private Merchant getApprovedMerchant(UUID merchantId) {

        Merchant merchant = merchantRepository
                .findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));

        if (merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new RuntimeException(
                    "Merchant chưa được Admin phê duyệt");
        }

        return merchant;
    }

    // Chuyển Entity thành Response DTO
    private FoodResponse mapToResponse(Food food) {

        FoodResponse response = new FoodResponse();

        response.setId(food.getId());

        if (food.getMerchantAddress() != null) {
            response.setMerchantAddressId(
                    food.getMerchantAddress().getId()
            );

            response.setMerchantAddress(
                    food.getMerchantAddress().getMerchantAddress()
            );

            response.setMerchantOpenTime(
                    food.getMerchantAddress().getMerchantOpenTime()
            );

            response.setMerchantCloseTime(
                    food.getMerchantAddress().getMerchantCloseTime()
            );
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

        // Category
        if (food.getFoodCategories() != null) {

            response.setCategoryIds(
                    food.getFoodCategories()
                            .stream()
                            .map(FoodCategory::getId)
                            .toList()
            );

            response.setCategoryNames(
                    food.getFoodCategories()
                            .stream()
                            .map(FoodCategory::getCategoryName)
                            .toList()
            );
        }

        // Tag
        if (food.getTags() != null) {

            response.setTagIds(
                    food.getTags()
                            .stream()
                            .map(Tag::getId)
                            .toList()
            );

            response.setTagNames(
                    food.getTags()
                            .stream()
                            .map(Tag::getTagName)
                            .toList()
            );
        }

        // Image
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

    // Bật hoặc tắt đề xuất món ăn
    @Transactional
    public void toggleRecommendation(
            UUID merchantId,
            Long foodId) {

        Food food = foodRepository
                .findByIdAndMerchant_IdAndDeletedAtIsNull(
                        foodId,
                        merchantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy món ăn"));

        food.setIsRecommended(
                !Boolean.TRUE.equals(food.getIsRecommended())
        );

        foodRepository.save(food);
    }

    // Tìm kiếm gộp món ăn của Merchant theo Tên hoặc Địa chỉ
    @Transactional(readOnly = true)
    public Page<FoodResponse> searchMerchantFoodsByKeyword(
            UUID merchantId,
            String keyword,
            Pageable pageable) {

        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        String cleanKeyword = (keyword != null) ? keyword.trim() : "";

        // Gọi đúng tên phương thức mới trong Repository
        return foodRepository
                .searchMerchantFoodsByKeyword(merchantId, cleanKeyword, pageable)
                .map(this::mapToResponse);
    }
}