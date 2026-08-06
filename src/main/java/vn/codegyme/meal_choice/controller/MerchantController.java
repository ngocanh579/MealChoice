package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.MerchantRegisterRequest;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.service.MerchantService;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    public ResponseEntity<String> registerMerchant(
            @Valid @RequestBody MerchantRegisterRequest request) {

        merchantService.registerMerchant(request);

        return ResponseEntity.ok("Đăng ký thành công");
    }
}
