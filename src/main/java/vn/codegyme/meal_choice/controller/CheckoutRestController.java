package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.order.CheckoutRequestDTO;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.UserOrderService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutRestController {

    private final UserOrderService userOrderService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return null;
        }

        UUID userId = userDetails.getId();
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * REST API: ĐẶT HÀNG / THANH TOÁN
     */
    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody CheckoutRequestDTO request) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập trước khi thanh toán"
                ));
            }

            OrderResponseDTO response = userOrderService.placeOrder(user.getId(), request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt hàng thành công!",
                    "orderCode", response.getOrderCode(),
                    "orderId", response.getId(),
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý đặt hàng API: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
