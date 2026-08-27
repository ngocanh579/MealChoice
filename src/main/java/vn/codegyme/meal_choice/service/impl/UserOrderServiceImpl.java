package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.Delivery.GeoPoint;
import vn.codegyme.meal_choice.dto.order.CheckoutItemDTO;
import vn.codegyme.meal_choice.dto.order.CheckoutRequestDTO;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.mapper.OrderMapper;
import vn.codegyme.meal_choice.repository.*;
import vn.codegyme.meal_choice.service.DistanceService;
import vn.codegyme.meal_choice.service.GeocodingService;
import vn.codegyme.meal_choice.service.ShippingFeeService;
import vn.codegyme.meal_choice.service.UserOrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrderServiceImpl implements UserOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final FoodRepository foodRepository;
    private final OrderMapper orderMapper;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final ShippingFeeService shippingFeeService;
    private final DistanceService distanceService;
    private final GeocodingService geocodingService;

    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(15000);

    /**
     * Tạo đơn hàng mới từ giỏ hàng.
     */
    @Override
    @Transactional
    public OrderResponseDTO placeOrder(UUID userId, CheckoutRequestDTO request) {
        log.info("Bắt đầu đặt hàng cho user: {}, merchant: {}", userId, request.getMerchantId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin cửa hàng"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 món ăn");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal maxServiceFee = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CheckoutItemDTO itemDto : request.getItems()) {
            if (itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
            }

            Food food = foodRepository.findById(itemDto.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy món ăn ID: " + itemDto.getFoodId()
                    ));

            if (food.getMerchant() == null
                    || !food.getMerchant().getId().equals(merchant.getId())) {
                throw new IllegalArgumentException(
                        "Món ăn '" + food.getFoodName() + "' không thuộc cửa hàng này!"
                );
            }

            if (!Boolean.TRUE.equals(food.getIsActive()) || food.getDeletedAt() != null) {
                throw new IllegalArgumentException(
                        "Món ăn '" + food.getFoodName() + "' hiện không còn phục vụ!"
                );
            }

            BigDecimal unitPrice = food.getDiscountPrice() != null
                    && food.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                    ? food.getDiscountPrice()
                    : food.getPrice();

            BigDecimal itemSubtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemDto.getQuantity())
            );

            subtotal = subtotal.add(itemSubtotal);

            if (food.getServiceFee() != null
                    && food.getServiceFee().compareTo(maxServiceFee) > 0) {
                maxServiceFee = food.getServiceFee();
            }

            OrderItem orderItem = OrderItem.builder()
                    .food(food)
                    .foodName(food.getFoodName())
                    .foodImage(findPrimaryFoodImage(food))
                    .price(unitPrice)
                    .quantity(itemDto.getQuantity())
                    .subtotal(itemSubtotal)
                    .note(itemDto.getNote())
                    .build();

            orderItems.add(orderItem);

            food.setOrderCount(
                    (food.getOrderCount() != null ? food.getOrderCount() : 0)
                            + itemDto.getQuantity()
            );

            foodRepository.save(food);
        }

        DeliveryPartner deliveryPartner = null;
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;
        double distanceKm = 3.0;

        if (request.getDeliveryPartnerId() != null) {
            deliveryPartner = deliveryPartnerRepository
                    .findById(request.getDeliveryPartnerId())
                    .orElse(null);
        }

        if (deliveryPartner == null) {
            List<DeliveryPartner> activePartners =
                    deliveryPartnerRepository.findByStatus(DeliveryPartnerStatus.ACTIVE);

            if (!activePartners.isEmpty()) {
                deliveryPartner = activePartners.get(0);
            }
        }

        if (deliveryPartner != null) {
            try {
                List<MerchantAddress> merchantAddresses =
                        merchantAddressRepository.findByMerchantId(merchant.getId());

                if (!merchantAddresses.isEmpty()
                        && request.getDeliveryAddress() != null
                        && !request.getDeliveryAddress().trim().isEmpty()) {

                    MerchantAddress merchantAddress = merchantAddresses.get(0);

                    GeoPoint merchantPoint;

                    if (merchantAddress.getLatitude() != null
                            && merchantAddress.getLongitude() != null) {
                        merchantPoint = new GeoPoint(
                                merchantAddress.getLatitude(),
                                merchantAddress.getLongitude()
                        );
                    } else {
                        merchantPoint = geocodingService.geocode(
                                merchantAddress.getMerchantAddress() + ", Việt Nam"
                        );
                    }

                    GeoPoint userPoint = geocodingService.geocode(
                            request.getDeliveryAddress() + ", Việt Nam"
                    );

                    if (merchantPoint != null && userPoint != null) {
                        distanceKm = distanceService.calculateDistanceKm(
                                merchantPoint,
                                userPoint
                        );

                        String deliveryAddress =
                                request.getDeliveryAddress().toLowerCase();

                        String merchantAddressText =
                                merchantAddress.getMerchantAddress().toLowerCase();

                        if (deliveryAddress.contains("hà nội")
                                && merchantAddressText.contains("hà nội")
                                && distanceKm > 35.0) {
                            distanceKm = 3.0;
                        }
                    }
                }

                shippingFee = shippingFeeService.calculateShippingFee(
                        deliveryPartner,
                        distanceKm
                );
            } catch (Exception e) {
                log.warn(
                        "Không tính được phí ship chính xác qua API, dùng cước cơ bản: {}",
                        e.getMessage()
                );

                shippingFee = deliveryPartner.getBaseFee() != null
                        ? deliveryPartner.getBaseFee()
                        : DEFAULT_SHIPPING_FEE;
            }
        }

        BigDecimal serviceFee = maxServiceFee;
        BigDecimal discountAmount = calculateVoucherDiscount(
                request.getVoucherCode(),
                subtotal
        );

        BigDecimal totalAmount = subtotal
                .add(shippingFee)
                .add(serviceFee)
                .subtract(discountAmount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();

        int prepMinutes = 10;

        if (!orderItems.isEmpty()) {
            int maxPrep = orderItems.stream()
                    .mapToInt(item ->
                            item.getFood() != null
                                    && item.getFood().getPreparationTime() != null
                                    && item.getFood().getPreparationTime() > 0
                                    ? item.getFood().getPreparationTime()
                                    : 10
                    )
                    .max()
                    .orElse(10);

            prepMinutes = Math.max(5, maxPrep);
        }

        int deliveryTransitMinutes =
                (int) Math.max(4, Math.round(distanceKm * 4));

        int totalEstimatedMinutes =
                prepMinutes + deliveryTransitMinutes;

        LocalDateTime estimatedDelivery =
                now.plusMinutes(totalEstimatedMinutes);

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .merchant(merchant)
                .deliveryPartner(deliveryPartner)
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .paymentMethod(
                        request.getPaymentMethod() != null
                                ? request.getPaymentMethod()
                                : PaymentMethod.COD
                )
                .subtotalPrice(subtotal)
                .shippingFee(shippingFee)
                .serviceFee(serviceFee)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .estimatedDeliveryTime(estimatedDelivery)
                .createdAt(now)
                .updatedAt(now)
                .build();

        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Đặt hàng thành công, mã đơn: {}",
                savedOrder.getOrderCode()
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của User.
     */
    @Override
    @Transactional
    public List<OrderResponseDTO> getUserOrders(UUID userId) {
        List<Order> orders =
                orderRepository.findByUser_IdOrderByIdDesc(userId);

        for (Order order : orders) {
            autoSyncOrderStatus(order);
        }

        return orderMapper.toOrderResponseDTOList(orders);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của User có phân trang.
     */
    @Override
    @Transactional
    public Page<OrderResponseDTO> getUserOrders(
            UUID userId,
            Pageable pageable
    ) {
        Page<Order> orderPage =
                orderRepository.findByUser_IdOrderByIdDesc(
                        userId,
                        pageable
                );

        for (Order order : orderPage.getContent()) {
            autoSyncOrderStatus(order);
        }

        return orderMapper.toOrderResponseDTOPage(orderPage);
    }

    /**
     * Xem chi tiết đơn hàng theo mã đơn.
     */
    @Override
    @Transactional
    public OrderResponseDTO getOrderDetailByCode(String orderCode) {
        Order order = orderRepository
                .findByOrderCodeWithItems(orderCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn hàng với mã: " + orderCode
                ));

        autoSyncOrderStatus(order);

        return orderMapper.toOrderResponseDTO(order);
    }

    /**
     * Tự động chuyển đơn từ PREPARING sang DELIVERING
     * khi hết thời gian chuẩn bị món.
     */
    private void autoSyncOrderStatus(Order order) {
        if (order == null) {
            return;
        }

        if (order.getStatus() == OrderStatus.PREPARING
                && order.getPreparingUntil() != null
                && LocalDateTime.now().isAfter(order.getPreparingUntil())) {

            order.setStatus(OrderStatus.DELIVERING);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            log.info(
                    "Tự động chuyển đơn hàng ID {} sang DELIVERING do hết thời gian chuẩn bị",
                    order.getId()
            );
        }
    }

    /**
     * Tính số tiền giảm giá theo mã Voucher.
     */
    private BigDecimal calculateVoucherDiscount(
            String voucherCode,
            BigDecimal subtotal
    ) {
        if (voucherCode == null
                || voucherCode.trim().isEmpty()
                || subtotal == null
                || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        String code = voucherCode.trim().toUpperCase();

        switch (code) {
            case "GIAM10K":
                return BigDecimal.valueOf(10000);

            case "GIAM20K":
                return BigDecimal.valueOf(20000);

            case "GIAM50K":
                return BigDecimal.valueOf(50000);

            case "GIAM10%":
            case "GIAM10PT":
                return subtotal
                        .multiply(BigDecimal.valueOf(10))
                        .divide(BigDecimal.valueOf(100));

            case "GIAM20%":
            case "GIAM20PT":
                return subtotal
                        .multiply(BigDecimal.valueOf(20))
                        .divide(BigDecimal.valueOf(100));

            case "GIAM50%":
            case "GIAM50PT":
                return subtotal
                        .multiply(BigDecimal.valueOf(50))
                        .divide(BigDecimal.valueOf(100));

            default:
                return BigDecimal.valueOf(10000);
        }
    }

    /**
     * Lấy ảnh đại diện chính của món ăn.
     */
    private String findPrimaryFoodImage(Food food) {
        if (food.getImages() == null || food.getImages().isEmpty()) {
            return null;
        }

        return food.getImages()
                .stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(FoodImage::getImageUrl)
                .findFirst()
                .orElse(food.getImages().get(0).getImageUrl());
    }

    /**
     * Sinh mã đơn hàng dạng MC-XXXXXXX-XXX.
     */
    private String generateOrderCode() {
        return "MC-"
                + System.currentTimeMillis() % 10000000
                + "-"
                + String.format("%03d", new Random().nextInt(1000));
    }
}