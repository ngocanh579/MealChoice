package vn.codegyme.meal_choice.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.codegyme.meal_choice.entity.PaymentMethod;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDTO {

    @NotNull(message = "Thông tin quán không được để trống")
    private UUID merchantId;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String contactName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    private String contactPhone;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String deliveryAddress;

    private String note;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String voucherCode;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    @Valid
    private List<CheckoutItemDTO> items;
}
