package vn.codegyme.meal_choice.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AdminMerchantResponse(
        UUID id,
        String ownerName,
        String restaurantName,
        String email,
        String phone,
        String address,
        LocalTime openTime,
        LocalTime closeTime,
        String status,
        boolean accountActive,
        boolean loyalPartner,
        String reviewNote,
        String reviewedBy,
        LocalDateTime reviewedAt
) {
}
