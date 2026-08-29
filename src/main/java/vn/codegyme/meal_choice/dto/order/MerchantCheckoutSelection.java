package vn.codegyme.meal_choice.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MerchantCheckoutSelection {

    @NotNull(message = "merchantId không được để trống")
    private UUID merchantId;

    // Không bắt buộc — nếu để trống, hệ thống tự chọn đối tác có phí ship rẻ nhất
    private UUID deliveryPartnerId;
}
