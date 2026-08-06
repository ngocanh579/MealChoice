package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MerchantDecisionRequest(
        @NotBlank(message = "Quyết định không được để trống")
        @Pattern(regexp = "(?i)APPROVE|REJECT", message = "Quyết định chỉ có thể là APPROVE hoặc REJECT")
        String decision,

        @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
        String reason
) {
}
