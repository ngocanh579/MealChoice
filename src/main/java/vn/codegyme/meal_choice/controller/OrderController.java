package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.order.*;
import vn.codegyme.meal_choice.entity.OrderStatus;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.OrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== ĐẶT HÀNG ====================

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(currentUserId(authentication), request));
    }

    // ==================== DANH SÁCH ĐƠN HÀNG CỦA TÔI ====================

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(orderService.getMyOrders(currentUserId(authentication), status, pageable));
    }

    // ==================== CHI TIẾT ĐƠN HÀNG ====================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            Authentication authentication,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(currentUserId(authentication), orderId));
    }

    // ==================== HỦY ĐƠN HÀNG ====================

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(currentUserId(authentication), orderId, request));
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new RuntimeException("Chưa đăng nhập");
    }
}
