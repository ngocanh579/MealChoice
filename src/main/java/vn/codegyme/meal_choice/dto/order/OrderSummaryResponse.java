package vn.codegyme.meal_choice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID id;
    private String orderCode;

    private UUID merchantId;
    private String merchantName;

    private int totalItemQuantity; // tổng số lượng món trong đơn
    private BigDecimal totalAmount;

    private String status;
    private LocalDateTime createdAt;
}
