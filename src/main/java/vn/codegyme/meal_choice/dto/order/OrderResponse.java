package vn.codegyme.meal_choice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private String orderCode;

    private UUID merchantId;
    private String merchantName;
    private String merchantAddress;

    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;

    private String deliveryPartnerName;
    private Double distanceKm;

    private List<OrderItemResponse> items;

    private BigDecimal subtotal;
    private BigDecimal serviceFee;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    private String paymentMethod;
    private String paymentStatus;

    private String status;
    private boolean cancellable;

    private String note;
    private String cancelReason;
    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
