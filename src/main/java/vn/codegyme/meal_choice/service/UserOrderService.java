package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.dto.order.CheckoutRequestDTO;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service giao diện xử lý các nghiệp vụ đặt hàng và lịch sử đơn của Khách hàng (User)
 */
public interface UserOrderService {

    /**
     * TÍNH NĂNG 4: Tạo đơn hàng mới từ giỏ hàng (Checkout)
     */
    OrderResponseDTO placeOrder(UUID userId, CheckoutRequestDTO request);

    /**
     * Lấy danh sách lịch sử đơn hàng của User
     */
    List<OrderResponseDTO> getUserOrders(UUID userId);

    /**
     * Xem chi tiết đơn hàng theo mã đơn (Phục vụ trang Đặt hàng thành công)
     */
    OrderResponseDTO getOrderDetailByCode(String orderCode);
}
