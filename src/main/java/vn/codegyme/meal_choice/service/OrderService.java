package vn.codegyme.meal_choice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.codegyme.meal_choice.dto.order.CancelOrderRequest;
import vn.codegyme.meal_choice.dto.order.OrderResponse;
import vn.codegyme.meal_choice.dto.order.OrderSummaryResponse;
import vn.codegyme.meal_choice.dto.order.PlaceOrderRequest;
import vn.codegyme.meal_choice.dto.order.PlaceOrderResponse;
import vn.codegyme.meal_choice.entity.OrderStatus;

import java.util.UUID;

public interface OrderService {

    /**
     * Đặt hàng từ giỏ hàng hiện tại của user. Mỗi quán (merchant) trong giỏ
     * (hoặc trong danh sách merchantSelections nếu được truyền) sẽ tạo thành
     * một Order riêng biệt.
     */
    PlaceOrderResponse placeOrder(UUID userId, PlaceOrderRequest request);

    /**
     * Danh sách đơn hàng của user, mới nhất trước, có thể lọc theo trạng thái.
     */
    Page<OrderSummaryResponse> getMyOrders(UUID userId, OrderStatus status, Pageable pageable);

    /**
     * Chi tiết 1 đơn hàng — chỉ trả về nếu đơn thuộc về user đang đăng nhập.
     */
    OrderResponse getOrderDetail(UUID userId, UUID orderId);

    /**
     * Hủy đơn hàng — chỉ cho phép khi đơn đang ở trạng thái có thể hủy
     * (PENDING hoặc CONFIRMED) và thuộc về user đang đăng nhập.
     */
    OrderResponse cancelOrder(UUID userId, UUID orderId, CancelOrderRequest request);
}
