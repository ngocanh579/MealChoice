package vn.codegyme.meal_choice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {

    private List<OrderResponse> orders;
    private BigDecimal grandTotal; // tổng tiền của tất cả đơn vừa tạo
}
