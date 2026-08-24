package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // Phí giao hàng cố định mặc định dự phòng: 15.000 đ
    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(15000);

    /**
     * TÍNH NĂNG 4: Tạo đơn hàng mới từ giỏ hàng (Checkout)
     */
    @Override
    @Transactional
    public OrderResponseDTO placeOrder(UUID userId, CheckoutRequestDTO request) {
        log.info("Bắt đầu đặt hàng cho user: {}, merchant: {}", userId, request.getMerchantId());

        // BƯỚC 1: Kiểm tra thông tin người dùng và cửa hàng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin cửa hàng"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 món ăn");
        }

        // BƯỚC 2: Xử lý từng món ăn, tính tiền món và kiểm tra ràng buộc 1 cửa hàng
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal maxServiceFee = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CheckoutItemDTO itemDto : request.getItems()) {
            Food food = foodRepository.findById(itemDto.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn ID: " + itemDto.getFoodId()));

            // Ràng buộc: Món ăn phải thuộc đúng Merchant đang đặt hàng
            if (food.getMerchant() == null || !food.getMerchant().getId().equals(merchant.getId())) {
                throw new IllegalArgumentException("Món ăn '" + food.getFoodName() + "' không thuộc cửa hàng này!");
            }

            if (!Boolean.TRUE.equals(food.getIsActive()) || food.getDeletedAt() != null) {
                throw new IllegalArgumentException("Món ăn '" + food.getFoodName() + "' hiện không còn phục vụ!");
            }

            // Giá áp dụng: Ưu tiên giá giảm nếu có
            BigDecimal unitPrice = (food.getDiscountPrice() != null && food.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? food.getDiscountPrice()
                    : food.getPrice();

            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            // Cập nhật phí dịch vụ cao nhất giữa các món
            if (food.getServiceFee() != null && food.getServiceFee().compareTo(maxServiceFee) > 0) {
                maxServiceFee = food.getServiceFee();
            }

            // Tạo đối tượng OrderItem
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

            // Cập nhật số lượt đặt của món ăn
            food.setOrderCount((food.getOrderCount() != null ? food.getOrderCount() : 0) + itemDto.getQuantity());
            foodRepository.save(food);
        }

        // BƯỚC 3: Tính toán các loại phí (Phí ship động theo đơn vị vận chuyển), phí dịch vụ & voucher
        DeliveryPartner deliveryPartner = null;
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;
        double distanceKm = 3.0; // Mặc định 3km nếu không tính được

        if (request.getDeliveryPartnerId() != null) {
            deliveryPartner = deliveryPartnerRepository.findById(request.getDeliveryPartnerId()).orElse(null);
        }
        if (deliveryPartner == null) {
            List<DeliveryPartner> activePartners = deliveryPartnerRepository.findByStatus(DeliveryPartnerStatus.ACTIVE);
            if (!activePartners.isEmpty()) {
                deliveryPartner = activePartners.get(0);
            }
        }

        if (deliveryPartner != null) {
            try {
                List<MerchantAddress> merchantAddrs = merchantAddressRepository.findByMerchantId(merchant.getId());
                if (!merchantAddrs.isEmpty() && request.getDeliveryAddress() != null) {
                    MerchantAddress mAddr = merchantAddrs.get(0);
                    GeoPoint mPoint = (mAddr.getLatitude() != null && mAddr.getLongitude() != null)
                            ? new GeoPoint(mAddr.getLatitude(), mAddr.getLongitude())
                            : geocodingService.geocode(mAddr.getMerchantAddress() + ", Việt Nam");
                    GeoPoint uPoint = geocodingService.geocode(request.getDeliveryAddress() + ", Việt Nam");
                    if (mPoint != null && uPoint != null) {
                        distanceKm = distanceService.calculateDistanceKm(mPoint, uPoint);
                        if (request.getDeliveryAddress().toLowerCase().contains("hà nội")
                                && mAddr.getMerchantAddress().toLowerCase().contains("hà nội")
                                && distanceKm > 35.0) {
                            distanceKm = 3.0;
                        }
                    }
                }
                shippingFee = shippingFeeService.calculateShippingFee(deliveryPartner, distanceKm);
            } catch (Exception e) {
                log.warn("Không tính được phí ship chính xác qua API, dùng cước cơ bản: {}", e.getMessage());
                shippingFee = (deliveryPartner.getBaseFee() != null) ? deliveryPartner.getBaseFee() : DEFAULT_SHIPPING_FEE;
            }
        }

        BigDecimal serviceFee = maxServiceFee;
        BigDecimal discountAmount = calculateVoucherDiscount(request.getVoucherCode(), subtotal);

        BigDecimal totalAmount = subtotal.add(shippingFee).add(serviceFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // BƯỚC 4: Tạo thực thể Order và lưu vào cơ sở dữ liệu
        LocalDateTime now = LocalDateTime.now();
        int estimatedMinutes = (int) Math.max(15, Math.min(60, 20 + Math.round(distanceKm * 4)));
        LocalDateTime estimatedDelivery = now.plusMinutes(estimatedMinutes);

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
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD)
                .subtotalPrice(subtotal)
                .shippingFee(shippingFee)
                .serviceFee(serviceFee)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .estimatedDeliveryTime(estimatedDelivery)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Gán các món ăn vào đơn hàng
        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Đặt hàng thành công, mã đơn: {}", savedOrder.getOrderCode());

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của User
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(UUID userId) {
        List<Order> orders = orderRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return orderMapper.toOrderResponseDTOList(orders);
    }

    /**
     * Xem chi tiết đơn hàng theo mã đơn (Dùng cho trang Đặt hàng thành công)
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetailByCode(String orderCode) {
        Order order = orderRepository.findByOrderCodeWithItems(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));
        return orderMapper.toOrderResponseDTO(order);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Tính toán số tiền giảm giá dựa theo mã Voucher (hỗ trợ 2 loại: giảm số tiền và giảm theo %)
     */
    private BigDecimal calculateVoucherDiscount(String voucherCode, BigDecimal subtotal) {
        if (voucherCode == null || voucherCode.trim().isEmpty() || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        String code = voucherCode.trim().toUpperCase();
        switch (code) {
            // Loại 1: Giảm theo số tiền cố định
            case "GIAM10K":
                return BigDecimal.valueOf(10000);
            case "GIAM20K":
                return BigDecimal.valueOf(20000);
            case "GIAM50K":
                return BigDecimal.valueOf(50000);

            // Loại 2: Giảm theo phần trăm (%)
            case "GIAM10%":
            case "GIAM10PT":
                return subtotal.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100));
            case "GIAM20%":
            case "GIAM20PT":
                return subtotal.multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(100));
            case "GIAM50%":
            case "GIAM50PT":
                return subtotal.multiply(BigDecimal.valueOf(50)).divide(BigDecimal.valueOf(100));

            default:
                // Mặc định giảm 10.000 đ cho các mã hợp lệ khác
                return BigDecimal.valueOf(10000);
        }
    }

    /**
     * Lấy ảnh đại diện chính của món ăn
     */
    private String findPrimaryFoodImage(Food food) {
        if (food.getImages() == null || food.getImages().isEmpty()) {
            return null;
        }
        return food.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(FoodImage::getImageUrl)
                .findFirst()
                .orElse(food.getImages().get(0).getImageUrl());
    }

    /**
     * Sinh mã đơn hàng ngẫu nhiên: MC-XXXXXXX-XXX
     */
    private String generateOrderCode() {
        return "MC-" + System.currentTimeMillis() % 10000000 + "-" + String.format("%03d", new Random().nextInt(1000));
    }
}
