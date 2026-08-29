package vn.codegyme.meal_choice.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    private Long addressId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD | ONLINE

    private String note;

    // Không truyền hoặc để trống -> đặt hàng TOÀN BỘ giỏ (tất cả các quán).
    // Truyền danh sách -> chỉ đặt hàng những quán được chọn (các quán còn lại vẫn giữ trong giỏ).
    private List<MerchantCheckoutSelection> merchantSelections;
}
