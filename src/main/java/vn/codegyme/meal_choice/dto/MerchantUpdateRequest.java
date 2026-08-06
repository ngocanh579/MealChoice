package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class MerchantUpdateRequest {
    @NotBlank(message = "Tên nhà hàng không được để trống")
    private String restaurantName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotNull(message = "Giờ mở cửa không được để trống")
    private LocalTime openTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    private LocalTime closeTime;
}
