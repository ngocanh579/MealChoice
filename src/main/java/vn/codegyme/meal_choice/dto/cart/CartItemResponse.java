package vn.codegyme.meal_choice.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long cartItemId;

    private Long foodId;
    private String foodName;
    private String foodImageUrl;

    private BigDecimal unitPrice;     // Giá hiện tại (đã áp giá khuyến mãi nếu có)
    private BigDecimal originalPrice; // Giá gốc (null nếu không có khuyến mãi)

    private Integer quantity;
    private String note;

    private BigDecimal subtotal; // unitPrice * quantity

    // false nếu món đã bị merchant tắt hoạt động / xóa / quán bị khóa -> cần cảnh báo cho user trước khi đặt hàng
    private boolean available;
    private String unavailableReason;
}
