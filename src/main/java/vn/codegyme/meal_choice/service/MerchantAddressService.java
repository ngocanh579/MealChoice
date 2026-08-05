package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.MerchantAddressRequest;
import vn.codegyme.meal_choice.dto.MerchantAddressResponse;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantAddressService {

    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;


    // ==================== CREATE ====================

    @Transactional
    public void createAddress(
            UUID merchantId,
            MerchantAddressRequest request
    ) {

        // Bước 1: Tìm Merchant để xác định địa chỉ thuộc cửa hàng nào
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));

        // Bước 2: Tạo đối tượng MerchantAddress mới
        MerchantAddress address = new MerchantAddress();

        // Bước 3: Liên kết địa chỉ với Merchant
        address.setMerchant(merchant);

        // Bước 4: Lấy dữ liệu từ Request
        // và gán vào MerchantAddress
        address.setMerchantAddress(
                request.getMerchantAddress()
        );

        address.setMerchantOpenTime(
                request.getMerchantOpenTime()
        );

        address.setMerchantCloseTime(
                request.getMerchantCloseTime()
        );

        // Bước 5: Lưu địa chỉ vào Database
        merchantAddressRepository.save(address);
    }


    // ==================== READ ====================

    public List<MerchantAddressResponse> getAddresses(
            UUID merchantId
    ) {

        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("Không tìm thấy Merchant");
        }

        List<MerchantAddress> addresses =
                merchantAddressRepository.findByMerchantId(merchantId);

        return addresses.stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional
    public void updateAddress(
            UUID merchantId,
            UUID addressId,
            MerchantAddressRequest request
    ) {

        MerchantAddress address = merchantAddressRepository
                .findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy địa chỉ"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException(
                    "Địa chỉ không thuộc Merchant này"
            );
        }

        address.setMerchantAddress(
                request.getMerchantAddress()
        );

        address.setMerchantOpenTime(
                request.getMerchantOpenTime()
        );

        address.setMerchantCloseTime(
                request.getMerchantCloseTime()
        );

        merchantAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(
            UUID merchantId,
            UUID addressId
    ) {


        MerchantAddress address = merchantAddressRepository
                .findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy địa chỉ"));

        if (!address.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException(
                    "Địa chỉ không thuộc Merchant này"
            );
        }

        merchantAddressRepository.delete(address);
    }


    // ==================== MAPPER ====================

    // Chuyển MerchantAddress Entity thành MerchantAddressResponse
    private MerchantAddressResponse mapToResponse(
            MerchantAddress address
    ) {

        MerchantAddressResponse response =
                new MerchantAddressResponse();

        response.setId(address.getId());
        response.setMerchantAddress(
                address.getMerchantAddress()
        );
        response.setMerchantOpenTime(
                address.getMerchantOpenTime()
        );
        response.setMerchantCloseTime(
                address.getMerchantCloseTime()
        );

        return response;
    }
}