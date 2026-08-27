package vn.codegyme.meal_choice.mapper;

import org.springframework.stereotype.Component;
import vn.codegyme.meal_choice.dto.order.OrderItemResponseDTO;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        Merchant merchant = order.getMerchant();
        User user = order.getUser();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(order.getCreatedAt())
                .formattedCreatedAt(formatDateTime(order.getCreatedAt()))
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .formattedEstimatedDeliveryTime(formatDateTime(order.getEstimatedDeliveryTime()))
                .cancelReason(order.getCancelReason())
                .status(order.getStatus())
                .statusDisplayName(getStatusDisplayName(order.getStatus()))
                .statusBadgeClass(getStatusBadgeClass(order.getStatus()))
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDisplayName(getPaymentMethodDisplayName(order.getPaymentMethod()))
                .merchantId(merchant != null ? merchant.getId() : null)
                .merchantName(merchant != null ? merchant.getMerchantRestaurantName() : "")
                .merchantPhone(merchant != null ? merchant.getMerchantPhone() : "")
                .merchantAddress(getMerchantAddress(merchant))
                .merchantBankName(merchant != null ? merchant.getBankName() : "")
                .merchantBankAccountNumber(merchant != null ? merchant.getBankAccountNumber() : "")
                .userId(user != null ? user.getId() : null)
                .customerName(order.getContactName())
                .customerPhone(order.getContactPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .subtotalPrice(order.getSubtotalPrice())
                .shippingFee(order.getShippingFee())
                .serviceFee(order.getServiceFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .totalItems(calculateTotalQuantity(order.getOrderItems()))
                .items(toOrderItemResponseDTOList(order.getOrderItems()))
                .build();
    }

    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        Food food = item.getFood();

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .foodId(food != null ? food.getId() : null)
                .foodName(item.getFoodName())
                .foodImage(resolveFoodImage(item))
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .note(item.getNote())
                .build();
    }

    public List<OrderItemResponseDTO> toOrderItemResponseDTOList(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> toOrderResponseDTOList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toOrderResponseDTO)
                .collect(Collectors.toList());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null
                ? dateTime.format(DATE_TIME_FORMATTER)
                : "";
    }

    private String getStatusDisplayName(OrderStatus status) {
        return status != null
                ? status.getDisplayName()
                : "";
    }

    private String getStatusBadgeClass(OrderStatus status) {
        return status != null
                ? status.getBadgeClass()
                : "";
    }

    private String getPaymentMethodDisplayName(PaymentMethod paymentMethod) {
        return paymentMethod != null
                ? paymentMethod.getDisplayName()
                : "";
    }

    private int calculateTotalQuantity(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        return items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }

    private String getMerchantAddress(Merchant merchant) {
        if (merchant == null) {
            return "";
        }

        try {
            if (merchant.getAddresses() == null || merchant.getAddresses().isEmpty()) {
                return "";
            }

            MerchantAddress address = merchant.getAddresses().get(0);

            if (address == null || address.getMerchantAddress() == null) {
                return "";
            }

            return address.getMerchantAddress();
        } catch (Exception e) {
            return "";
        }
    }

    private String resolveFoodImage(OrderItem item) {
        if (item == null) {
            return "";
        }

        if (item.getFoodImage() != null && !item.getFoodImage().isBlank()) {
            return item.getFoodImage();
        }

        try {
            Food food = item.getFood();

            if (food != null
                    && food.getImages() != null
                    && !food.getImages().isEmpty()) {
                return food.getImages().get(0).getImageUrl();
            }
        } catch (Exception ignored) {
        }

        return "";
    }
}