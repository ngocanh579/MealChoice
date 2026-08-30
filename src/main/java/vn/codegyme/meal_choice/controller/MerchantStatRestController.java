package vn.codegyme.meal_choice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.service.impl.MerchantStatService;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchant/stats")
@CrossOrigin("*")
public class MerchantStatRestController {

    private final MerchantStatService statService;

    public MerchantStatRestController(MerchantStatService statService) {
        this.statService = statService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(@RequestParam UUID merchantId, @RequestParam(defaultValue = "MONTH") String type) {
        return ResponseEntity.ok(statService.getRevenueStats(merchantId, type));
    }

    @GetMapping("/foods")
    public ResponseEntity<?> getFoodStats(@RequestParam UUID merchantId) {
        return ResponseEntity.ok(statService.getFoodStats(merchantId));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomerStats(@RequestParam UUID merchantId) {
        return ResponseEntity.ok(statService.getCustomerStats(merchantId));
    }

    @GetMapping("/coupons")
    public ResponseEntity<?> getCouponStats(@RequestParam UUID merchantId) {
        return ResponseEntity.ok(statService.getCouponStats(merchantId));
    }
}