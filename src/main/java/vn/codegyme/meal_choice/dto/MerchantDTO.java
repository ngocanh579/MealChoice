package vn.codegyme.meal_choice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.codegyme.meal_choice.entity.Merchant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private boolean active;

    public static MerchantDTO fromEntity(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        return new MerchantDTO(
            merchant.getId(),
            merchant.getName(),
            merchant.getDescription(),
            merchant.getAddress(),
            merchant.getPhone(),
            merchant.getEmail(),
            merchant.isActive()
        );
    }
}
