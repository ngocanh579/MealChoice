package vn.codegyme.meal_choice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MerchantResponse {

    private UUID id;

    private String merchantRestaurantName;

    private String merchantEmail;

    private String merchantPhone;

    private String merchantStatus;

    private List<MerchantAddressResponse> addresses;
}