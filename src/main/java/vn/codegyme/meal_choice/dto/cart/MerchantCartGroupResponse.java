package vn.codegyme.meal_choice.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCartGroupResponse {

    private UUID merchantId;
    private String merchantName;
    private boolean merchantAvailable; // false nếu quán bị khóa/từ chối

    private List<CartItemResponse> items;

    private BigDecimal merchantSubtotal;   // Tổng tiền món của quán này
    private BigDecimal merchantServiceFee; // Tổng phí dịch vụ của quán này
}
