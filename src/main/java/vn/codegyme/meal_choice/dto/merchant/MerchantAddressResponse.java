package vn.codegyme.meal_choice.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class MerchantAddressResponse {

    private UUID id;

    private String merchantAddress;

    private LocalTime merchantOpenTime;

    private LocalTime merchantCloseTime;
}