package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.MerchantProfileResponse;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.service.MerchantProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantProfileController {

    private final MerchantProfileService merchantProfileService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        MerchantProfileResponse merchant = merchantProfileService.getCurrentMerchant();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy thông tin cửa hàng thành công",
                "data", merchant
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody MerchantUpdateRequest request
    ) {
        MerchantProfileResponse merchant = merchantProfileService.updateCurrentMerchant(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thông tin cửa hàng thành công",
                "data", merchant
        ));
    }
}
