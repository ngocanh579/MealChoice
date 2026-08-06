package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

@Data
public class MerchantUpdateRequest {

    @NotBlank(message = "Tên nhà hàng không được để trống")
    private String merchantRestaurantName;

    private String merchantAddress;

    private LocalTime merchantOpenTime;

    private LocalTime merchantCloseTime;
}