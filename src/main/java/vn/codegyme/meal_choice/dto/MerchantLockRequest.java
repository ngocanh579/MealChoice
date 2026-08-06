package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotNull;

public record MerchantLockRequest(
        @NotNull(message = "Trạng thái khóa không được để trống") Boolean locked
) {
}
