package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.MerchantAddressRequest;
import vn.codegyme.meal_choice.dto.MerchantAddressResponse;
import vn.codegyme.meal_choice.dto.MerchantRegisterRequest;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.service.MerchantAddressService;
import vn.codegyme.meal_choice.service.MerchantService;
import vn.codegyme.meal_choice.dto.MerchantResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantAddressService merchantAddressService;

    // Đăng ký Merchant
    @PostMapping("/register")
    public ResponseEntity<String> registerMerchant(
            @Valid @RequestBody MerchantRegisterRequest request) {

        merchantService.registerMerchant(request);

        return ResponseEntity.ok("Đăng ký thành công");
    }

    // Cập nhật thông tin Merchant
    @PutMapping("/{merchantId}")
    public ResponseEntity<String> updateMerchant(
            @PathVariable UUID merchantId,
            @Valid @RequestBody MerchantUpdateRequest request) {

        merchantService.updateMerchant(merchantId, request);

        return ResponseEntity.ok(
                "Cập nhật thông tin Merchant thành công"
        );
    }

    // Thêm địa chỉ Merchant
    @PostMapping("/{merchantId}/addresses")
    public ResponseEntity<String> createAddress(
            @PathVariable UUID merchantId,
            @Valid @RequestBody MerchantAddressRequest request) {

        merchantAddressService.createAddress(
                merchantId,
                request
        );

        return ResponseEntity.ok("Thêm địa chỉ thành công");
    }

    // Lấy danh sách địa chỉ Merchant
    @GetMapping("/{merchantId}/addresses")
    public ResponseEntity<List<MerchantAddressResponse>> getAddresses(
            @PathVariable UUID merchantId) {

        List<MerchantAddressResponse> addresses =
                merchantAddressService.getAddresses(merchantId);

        return ResponseEntity.ok(addresses);
    }

    // Cập nhật địa chỉ Merchant
    @PutMapping("/{merchantId}/addresses/{addressId}")
    public ResponseEntity<String> updateAddress(
            @PathVariable UUID merchantId,
            @PathVariable UUID addressId,
            @Valid @RequestBody MerchantAddressRequest request) {

        merchantAddressService.updateAddress(
                merchantId,
                addressId,
                request
        );

        return ResponseEntity.ok("Cập nhật địa chỉ thành công");
    }

    // Xóa địa chỉ Merchant
    @DeleteMapping("/{merchantId}/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable UUID merchantId,
            @PathVariable UUID addressId) {

        merchantAddressService.deleteAddress(
                merchantId,
                addressId
        );

        return ResponseEntity.ok("Xóa địa chỉ thành công");
    }

    // Lấy thông tin Merchant
    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchant(
            @PathVariable UUID merchantId) {

        MerchantResponse response =
                merchantService.getMerchant(merchantId);

        return ResponseEntity.ok(response);
    }
}