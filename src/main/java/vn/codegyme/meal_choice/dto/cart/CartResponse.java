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
public class CartResponse {

    private UUID cartId;
    private List<MerchantCartGroupResponse> merchantGroups;

    private int totalItems; // tổng số dòng món (không tính theo số lượng)
    private BigDecimal totalAmount; // tổng tiền món (chưa gồm phí ship, tính sau khi chọn địa chỉ)
}
