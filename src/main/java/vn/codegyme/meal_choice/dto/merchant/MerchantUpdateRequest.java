package vn.codegyme.meal_choice.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class MerchantUpdateRequest {

    @NotBlank(message = "Tên nhà hàng không được để trống")
    private String merchantRestaurantName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String merchantAddress;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String provinceCode;

    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String districtCode;

    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String wardCode;

    @NotNull(message = "Giờ mở cửa không được để trống")
    private LocalTime merchantOpenTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    private LocalTime merchantCloseTime;
}