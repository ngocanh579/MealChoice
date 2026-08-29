package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.cart.*;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.CartItemRepository;
import vn.codegyme.meal_choice.repository.CartRepository;
import vn.codegyme.meal_choice.repository.FoodRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.util.FoodPricingUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    // ==================== XEM GIỎ HÀNG ====================

    @Transactional
    public CartResponse getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartResponse(cart);
    }

    // ==================== TẠO GIỎ HÀNG / THÊM MÓN ====================

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        assertFoodOrderable(food);

        Optional<CartItem> existing = cartItemRepository.findByCart_IdAndFood_Id(cart.getId(), food.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (request.getNote() != null) {
                item.setNote(request.getNote());
            }
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .food(food)
                    .quantity(request.getQuantity())
                    .note(request.getNote())
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    // ==================== CHỈNH SỬA GIỎ HÀNG ====================

    @Transactional
    public CartResponse updateItem(UUID userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findByIdAndCart_Id(cartItemId, cart.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong giỏ hàng"));

        item.setQuantity(request.getQuantity());
        item.setNote(request.getNote());
        cartItemRepository.save(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findByIdAndCart_Id(cartItemId, cart.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong giỏ hàng"));

        cartItemRepository.delete(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart_Id(cart.getId());
        return toCartResponse(cart);
    }

    // ==================== HELPER ====================

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private void assertFoodOrderable(Food food) {
        if (Boolean.FALSE.equals(food.getIsActive()) || food.getDeletedAt() != null) {
            throw new RuntimeException("Món ăn hiện không khả dụng");
        }
        Merchant merchant = food.getMerchant();
        if (merchant == null || merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new RuntimeException("Quán ăn hiện không khả dụng");
        }
    }

    /**
     * Giá áp dụng hiện tại: ưu tiên discountPrice nếu có và nhỏ hơn giá gốc.
     */
    static BigDecimal effectivePrice(Food food) {
        return FoodPricingUtils.effectivePrice(food);
    }

    static String primaryImageUrl(Food food) {
        return FoodPricingUtils.primaryImageUrl(food);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart_IdOrderByCreatedAtDesc(cart.getId());

        Map<UUID, List<CartItem>> byMerchant = items.stream()
                .collect(Collectors.groupingBy(ci -> ci.getFood().getMerchant().getId(), LinkedHashMap::new, Collectors.toList()));

        List<MerchantCartGroupResponse> groups = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<UUID, List<CartItem>> entry : byMerchant.entrySet()) {
            List<CartItem> merchantItems = entry.getValue();
            Merchant merchant = merchantItems.get(0).getFood().getMerchant();

            boolean merchantAvailable = merchant.getMerchantStatus() == MerchantStatus.APPROVED;

            List<CartItemResponse> itemResponses = new ArrayList<>();
            BigDecimal merchantSubtotal = BigDecimal.ZERO;
            BigDecimal merchantServiceFee = BigDecimal.ZERO;

            for (CartItem ci : merchantItems) {
                Food food = ci.getFood();
                boolean foodAvailable = Boolean.TRUE.equals(food.getIsActive()) && food.getDeletedAt() == null;
                boolean available = foodAvailable && merchantAvailable;

                String unavailableReason = null;
                if (!foodAvailable) {
                    unavailableReason = "Món ăn hiện không khả dụng";
                } else if (!merchantAvailable) {
                    unavailableReason = "Quán ăn hiện không khả dụng";
                }

                BigDecimal unitPrice = effectivePrice(food);
                BigDecimal originalPrice = unitPrice.compareTo(food.getPrice()) < 0 ? food.getPrice() : null;
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));

                itemResponses.add(CartItemResponse.builder()
                        .cartItemId(ci.getId())
                        .foodId(food.getId())
                        .foodName(food.getFoodName())
                        .foodImageUrl(primaryImageUrl(food))
                        .unitPrice(unitPrice)
                        .originalPrice(originalPrice)
                        .quantity(ci.getQuantity())
                        .note(ci.getNote())
                        .subtotal(subtotal)
                        .available(available)
                        .unavailableReason(unavailableReason)
                        .build());

                if (available) {
                    merchantSubtotal = merchantSubtotal.add(subtotal);
                    merchantServiceFee = merchantServiceFee.add(
                            (food.getServiceFee() != null ? food.getServiceFee() : BigDecimal.ZERO)
                                    .multiply(BigDecimal.valueOf(ci.getQuantity())));
                }
            }

            groups.add(MerchantCartGroupResponse.builder()
                    .merchantId(merchant.getId())
                    .merchantName(merchant.getMerchantRestaurantName())
                    .merchantAvailable(merchantAvailable)
                    .items(itemResponses)
                    .merchantSubtotal(merchantSubtotal)
                    .merchantServiceFee(merchantServiceFee)
                    .build());

            grandTotal = grandTotal.add(merchantSubtotal);
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .merchantGroups(groups)
                .totalItems(items.size())
                .totalAmount(grandTotal)
                .build();
    }
}
