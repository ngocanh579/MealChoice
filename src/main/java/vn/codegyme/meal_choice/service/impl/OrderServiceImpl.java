package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.order.*;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.*;
import vn.codegyme.meal_choice.service.DeliveryQuoteService;
import vn.codegyme.meal_choice.service.OrderService;
import vn.codegyme.meal_choice.service.ShippingQuote;
import vn.codegyme.meal_choice.util.FoodPricingUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryQuoteService deliveryQuoteService;

    // ==================== ĐẶT HÀNG ====================

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(UUID userId, PlaceOrderRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Address address = addressRepository.findByIdAndUser_Id(request.getAddressId(), userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng"));

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng của bạn đang trống"));

        List<CartItem> allItems = cartItemRepository.findByCart_IdOrderByCreatedAtDesc(cart.getId());
        if (allItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống");
        }

        Map<UUID, List<CartItem>> byMerchant = allItems.stream()
                .collect(Collectors.groupingBy(ci -> ci.getFood().getMerchant().getId(), LinkedHashMap::new, Collectors.toList()));

        // Xác định các quán sẽ được checkout lần này + đối tác giao hàng được chọn cho từng quán (nếu có)
        Map<UUID, UUID> merchantToPartnerChoice = new LinkedHashMap<>();

        if (request.getMerchantSelections() != null && !request.getMerchantSelections().isEmpty()) {
            for (MerchantCheckoutSelection selection : request.getMerchantSelections()) {
                if (!byMerchant.containsKey(selection.getMerchantId())) {
                    throw new RuntimeException("Quán không có trong giỏ hàng của bạn");
                }
                merchantToPartnerChoice.put(selection.getMerchantId(), selection.getDeliveryPartnerId());
            }
        } else {
            // Không chọn -> checkout toàn bộ giỏ hàng
            for (UUID merchantId : byMerchant.keySet()) {
                merchantToPartnerChoice.put(merchantId, null);
            }
        }

        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<UUID, UUID> entry : merchantToPartnerChoice.entrySet()) {
            UUID merchantId = entry.getKey();
            UUID chosenPartnerId = entry.getValue();

            List<CartItem> merchantItems = byMerchant.get(merchantId);
            Order order = buildOrderForMerchant(user, address, merchantId, merchantItems, chosenPartnerId, paymentMethod, request.getNote());
            orderRepository.save(order);
            createdOrders.add(order);

            // Xóa các món đã đặt khỏi giỏ hàng
            cartItemRepository.deleteByCart_IdAndFood_Merchant_Id(cart.getId(), merchantId);
        }

        List<OrderResponse> orderResponses = createdOrders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = createdOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PlaceOrderResponse.builder()
                .orders(orderResponses)
                .grandTotal(grandTotal)
                .build();
    }

    private Order buildOrderForMerchant(
            User user,
            Address address,
            UUID merchantId,
            List<CartItem> merchantItems,
            UUID chosenPartnerId,
            PaymentMethod paymentMethod,
            String note
    ) {
        Merchant merchant = merchantItems.get(0).getFood().getMerchant();

        if (merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new RuntimeException("Quán '" + merchant.getMerchantRestaurantName() + "' hiện không khả dụng, vui lòng cập nhật giỏ hàng");
        }

        for (CartItem ci : merchantItems) {
            Food food = ci.getFood();
            if (Boolean.FALSE.equals(food.getIsActive()) || food.getDeletedAt() != null) {
                throw new RuntimeException("Món '" + food.getFoodName() + "' hiện không khả dụng, vui lòng cập nhật giỏ hàng");
            }
        }

        // ===== Tính phí ship =====
        List<ShippingQuote> quotes = deliveryQuoteService.getQuotes(merchantId, address.getId());
        if (quotes.isEmpty()) {
            throw new RuntimeException("Hiện không có đối tác giao hàng khả dụng");
        }

        ShippingQuote quote;
        if (chosenPartnerId != null) {
            quote = quotes.stream()
                    .filter(q -> q.partnerId().equals(chosenPartnerId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Đối tác giao hàng đã chọn không khả dụng"));
        } else {
            quote = quotes.stream()
                    .min(Comparator.comparing(ShippingQuote::shippingFee))
                    .orElseThrow(() -> new RuntimeException("Hiện không có đối tác giao hàng khả dụng"));
        }

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(quote.partnerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác giao hàng"));

        // ===== Địa chỉ quán (để snapshot) =====
        List<MerchantAddress> merchantAddresses = merchantAddressRepository.findByMerchantId(merchantId);
        MerchantAddress merchantAddress = merchantAddresses.isEmpty() ? null : merchantAddresses.get(0);

        // ===== Tính tiền =====
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal serviceFee = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : merchantItems) {
            Food food = ci.getFood();
            BigDecimal unitPrice = FoodPricingUtils.effectivePrice(food);
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            BigDecimal lineServiceFee = (food.getServiceFee() != null ? food.getServiceFee() : BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(ci.getQuantity()));

            subtotal = subtotal.add(lineSubtotal);
            serviceFee = serviceFee.add(lineServiceFee);

            orderItems.add(OrderItem.builder()
                    .food(food)
                    .foodNameSnapshot(food.getFoodName())
                    .foodImageSnapshot(FoodPricingUtils.primaryImageUrl(food))
                    .unitPrice(unitPrice)
                    .quantity(ci.getQuantity())
                    .subtotal(lineSubtotal)
                    .note(ci.getNote())
                    .build());
        }

        BigDecimal shippingFee = quote.shippingFee();
        BigDecimal totalAmount = subtotal.add(serviceFee).add(shippingFee);

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .merchant(merchant)
                .merchantNameSnapshot(merchant.getMerchantRestaurantName())
                .merchantAddressSnapshot(merchantAddress != null ? merchantAddress.getMerchantAddress() : null)
                .merchantLatitude(merchantAddress != null ? merchantAddress.getLatitude() : null)
                .merchantLongitude(merchantAddress != null ? merchantAddress.getLongitude() : null)
                .receiverName(address.getContactName())
                .receiverPhone(address.getContactPhone())
                .deliveryAddressSnapshot(address.getFullAddress())
                .deliveryLatitude(address.getLatitude())
                .deliveryLongitude(address.getLongitude())
                .deliveryPartner(deliveryPartner)
                .deliveryPartnerNameSnapshot(deliveryPartner.getPartnerName())
                .distanceKm(quote.distanceKm())
                .subtotal(subtotal)
                .serviceFee(serviceFee)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.PENDING)
                .note(note)
                .build();

        orderItems.forEach(oi -> oi.setOrder(order));
        order.setItems(orderItems);

        return order;
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "OD" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private PaymentMethod parsePaymentMethod(String raw) {
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        }
    }

    // ==================== DANH SÁCH ĐƠN HÀNG ====================

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(UUID userId, OrderStatus status, Pageable pageable) {
        Page<Order> page = (status != null)
                ? orderRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable)
                : orderRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

        return page.map(this::toOrderSummaryResponse);
    }

    // ==================== CHI TIẾT ĐƠN HÀNG ====================

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return toOrderResponse(order);
    }

    // ==================== HỦY ĐƠN HÀNG ====================

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID userId, UUID orderId, CancelOrderRequest request) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.isCancellable()) {
            throw new RuntimeException("Đơn hàng không thể hủy ở trạng thái hiện tại");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request != null ? request.getReason() : null);
        order.setCancelledAt(LocalDateTime.now());

        orderRepository.save(order);
        return toOrderResponse(order);
    }

    // ==================== MAPPER ====================

    private OrderSummaryResponse toOrderSummaryResponse(Order order) {
        int totalQuantity = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();

        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .merchantId(order.getMerchant().getId())
                .merchantName(order.getMerchantNameSnapshot())
                .totalItemQuantity(totalQuantity)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .foodId(oi.getFood() != null ? oi.getFood().getId() : null)
                        .foodName(oi.getFoodNameSnapshot())
                        .foodImageUrl(oi.getFoodImageSnapshot())
                        .unitPrice(oi.getUnitPrice())
                        .quantity(oi.getQuantity())
                        .subtotal(oi.getSubtotal())
                        .note(oi.getNote())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .merchantId(order.getMerchant().getId())
                .merchantName(order.getMerchantNameSnapshot())
                .merchantAddress(order.getMerchantAddressSnapshot())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .deliveryAddress(order.getDeliveryAddressSnapshot())
                .deliveryPartnerName(order.getDeliveryPartnerNameSnapshot())
                .distanceKm(order.getDistanceKm())
                .items(items)
                .subtotal(order.getSubtotal())
                .serviceFee(order.getServiceFee())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .status(order.getStatus().name())
                .cancellable(order.isCancellable())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
