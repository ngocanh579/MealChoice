package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.*;
import vn.codegyme.meal_choice.service.AdminMerchantManagementService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    private final AdminMerchantManagementService adminMerchantService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        List<AdminMerchantResponse> merchants = adminMerchantService.findAll(status, keyword);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách merchant thành công",
                "data", merchants
        ));
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy chi tiết merchant thành công",
                "data", adminMerchantService.findById(merchantId)
        ));
    }

    @PatchMapping("/{merchantId}/decision")
    public ResponseEntity<Map<String, Object>> decide(
            @PathVariable UUID merchantId,
            @Valid @RequestBody MerchantDecisionRequest request
    ) {
        return success("Đã cập nhật quyết định xét duyệt", adminMerchantService.decide(merchantId, request));
    }

    @PatchMapping("/{merchantId}/lock")
    public ResponseEntity<Map<String, Object>> lock(
            @PathVariable UUID merchantId,
            @Valid @RequestBody MerchantLockRequest request
    ) {
        String message = request.locked() ? "Đã khóa merchant" : "Đã mở khóa merchant";
        return success(message, adminMerchantService.setLocked(merchantId, request.locked()));
    }

    @PatchMapping("/{merchantId}/loyal-partner")
    public ResponseEntity<Map<String, Object>> loyalPartner(
            @PathVariable UUID merchantId,
            @Valid @RequestBody LoyalPartnerRequest request
    ) {
        String message = request.approved()
                ? "Đã duyệt đối tác thân thiết"
                : "Đã hủy trạng thái đối tác thân thiết";
        return success(message, adminMerchantService.setLoyalPartner(merchantId, request.approved()));
    }

    private ResponseEntity<Map<String, Object>> success(String message, AdminMerchantResponse merchant) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "data", merchant
        ));
    }
}
