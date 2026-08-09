package vn.codegyme.meal_choice.dto;

import lombok.Data;
import vn.codegyme.meal_choice.entity.MerchantStatus;

import java.util.List;
import java.util.UUID;

@Data
public class MerchantResponse {

    private UUID id;

    private String merchantRestaurantName;

    private String merchantEmail;

    private String merchantPhone;

    private MerchantStatus merchantStatus;

    private List<MerchantAddressResponse> addresses;
}