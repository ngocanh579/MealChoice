package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotNull;

public record LoyalPartnerRequest(
        @NotNull(message = "Trạng thái đối tác thân thiết không được để trống") Boolean approved
) {
}
