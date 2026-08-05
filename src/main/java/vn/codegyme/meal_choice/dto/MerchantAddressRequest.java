package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class MerchantAddressRequest {

    @NotBlank(message = "Địa chỉ không được để trống")
    private String merchantAddress;

    @NotNull(message = "Giờ mở cửa không được để trống")
    private LocalTime merchantOpenTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    private LocalTime merchantCloseTime;
}