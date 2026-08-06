package vn.codegyme.meal_choice.dto;

import java.time.LocalTime;
import java.util.UUID;

public record MerchantProfileResponse(
        UUID id,
        String ownerName,
        String restaurantName,
        String email,
        String phone,
        String address,
        LocalTime openTime,
        LocalTime closeTime,
        String status,
        boolean loyalPartner,
        boolean accountActive
) {
}
