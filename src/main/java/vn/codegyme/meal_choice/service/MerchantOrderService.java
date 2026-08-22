package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service giao diện xử lý các nghiệp vụ quản lý đơn hàng dành riêng cho Merchant (Chủ quán)
 */
public interface MerchantOrderService {

    /**
     * TÍNH NĂNG 1: Lấy danh sách đơn hàng của quán (hỗ trợ lọc theo trạng thái)
     */
    List<OrderResponseDTO> getMerchantOrders(UUID merchantId, OrderStatus status, String search);

    /**
     * TÍNH NĂNG 2: Xem chi tiết một đơn hàng của quán
     */
    OrderResponseDTO getMerchantOrderDetail(UUID merchantId, Long orderId);

    /**
     * TÍNH NĂNG 1: Merchant bấm "Nhận đơn" (Chuyển sang PREPARING)
     */
    OrderResponseDTO acceptOrder(UUID merchantId, Long orderId);

    /**
     * TÍNH NĂNG 3: Merchant bấm "Hủy đơn" (Chuyển sang CANCELLED kèm lý do)
     */
    OrderResponseDTO cancelOrderByMerchant(UUID merchantId, Long orderId, String cancelReason);

    /**
     * Merchant bấm "Đã nhận tiền & Hoàn thành" (Chuyển sang COMPLETED)
     */
    OrderResponseDTO completeOrder(UUID merchantId, Long orderId);

    /**
     * Merchant bấm "Khách không nhận hàng" (Chuyển sang CANCELLED)
     */
    OrderResponseDTO markFailedDelivery(UUID merchantId, Long orderId, String cancelReason);

    /**
     * Đếm số lượng đơn hàng đang chờ tiếp nhận (PENDING)
     */
    long countPendingOrders(UUID merchantId);

    /**
     * Đếm số lượng đơn hàng theo trạng thái cụ thể (Task 13)
     */
    long countOrdersByStatus(UUID merchantId, OrderStatus status);
}
