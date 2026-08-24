package vn.codegyme.meal_choice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.Delivery.GeoPoint;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.AddressRepository;
import vn.codegyme.meal_choice.repository.DeliveryPartnerRepository;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryQuoteService {

    private final AddressRepository addressRepository;

    private final MerchantAddressRepository
            merchantAddressRepository;

    private final DeliveryPartnerRepository
            deliveryPartnerRepository;

    private final GeocodingService geocodingService;

    private final DistanceService distanceService;

    private final ShippingFeeService shippingFeeService;


    @Transactional
    public List<ShippingQuote> getQuotes(
            UUID merchantId,
            Long userAddressId
    ) {

        // =========================================
        // 1. ĐỊA CHỈ USER
        // =========================================

        Address userAddress =
                addressRepository
                        .findById(userAddressId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy địa chỉ người nhận."
                                )
                        );


        // =========================================
        // 2. ĐỊA CHỈ MERCHANT
        // =========================================

        List<MerchantAddress> merchantAddresses =
                merchantAddressRepository
                        .findByMerchantId(
                                merchantId
                        );


        if (merchantAddresses.isEmpty()) {

            throw new RuntimeException(
                    "Merchant chưa có địa chỉ."
            );
        }


        MerchantAddress merchantAddress =
                merchantAddresses.get(0);


        // =========================================
        // 3. TỌA ĐỘ USER
        // =========================================

        GeoPoint userPoint;


        if (userAddress.getLatitude() == null
                || userAddress.getLongitude() == null) {

            userPoint =
                    geocodingService.geocode(
                            userAddress.getFullAddress()
                                    + ", Việt Nam"
                    );


            userAddress.setLatitude(
                    userPoint.latitude()
            );

            userAddress.setLongitude(
                    userPoint.longitude()
            );


            addressRepository.save(
                    userAddress
            );

        } else {

            userPoint =
                    new GeoPoint(
                            userAddress.getLatitude(),
                            userAddress.getLongitude()
                    );
        }


        // =========================================
        // 4. TỌA ĐỘ MERCHANT
        // =========================================

        GeoPoint merchantPoint;


        if (merchantAddress.getLatitude() == null
                || merchantAddress.getLongitude() == null) {

            merchantPoint =
                    geocodingService.geocode(
                            merchantAddress
                                    .getMerchantAddress()
                                    + ", Việt Nam"
                    );


            merchantAddress.setLatitude(
                    merchantPoint.latitude()
            );

            merchantAddress.setLongitude(
                    merchantPoint.longitude()
            );


            merchantAddressRepository.save(
                    merchantAddress
            );

        } else {

            merchantPoint =
                    new GeoPoint(
                            merchantAddress.getLatitude(),
                            merchantAddress.getLongitude()
                    );
        }


        // =========================================
        // 5. TÍNH KHOẢNG CÁCH
        // =========================================

        double distanceKm =
                distanceService
                        .calculateDistanceKm(
                                merchantPoint,
                                userPoint
                        );


        // =========================================
        // 6. LẤY PARTNER ACTIVE
        // =========================================

        List<DeliveryPartner> partners =
                deliveryPartnerRepository
                        .findByStatus(
                                DeliveryPartnerStatus.ACTIVE
                        );


        // =========================================
        // 7. TÍNH GIÁ TỪNG PARTNER
        // =========================================

        return partners
                .stream()
                .map(partner -> {

                    BigDecimal fee =
                            shippingFeeService
                                    .calculateShippingFee(
                                            partner,
                                            distanceKm
                                    );


                    return new ShippingQuote(
                            partner.getId(),
                            partner.getPartnerName(),
                            distanceKm,
                            fee,
                            shippingFeeService
                                    .isPeakHour()
                    );
                })
                .toList();
    }
}
