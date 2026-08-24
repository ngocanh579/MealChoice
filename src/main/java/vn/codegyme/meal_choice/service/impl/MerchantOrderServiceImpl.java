package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.Order;
import vn.codegyme.meal_choice.entity.OrderStatus;
import vn.codegyme.meal_choice.mapper.OrderMapper;
import vn.codegyme.meal_choice.repository.OrderRepository;
import vn.codegyme.meal_choice.service.MerchantOrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * TÍNH NĂNG 1: Lấy danh sách đơn hàng của Merchant (có thể lọc theo trạng thái)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMerchantOrders(UUID merchantId, OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByMerchant_IdAndStatusOrderByCreatedAtDesc(merchantId, status);
        } else {
            orders = orderRepository.findByMerchant_IdOrderByCreatedAtDesc(merchantId);
        }
        return orderMapper.toOrderResponseDTOList(orders);
    }

    /**
     * TÍNH NĂNG 2: Xem chi tiết một đơn hàng của Merchant
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getMerchantOrderDetail(UUID merchantId, Long orderId) {
        Order order = orderRepository.findByIdAndMerchantIdWithItems(orderId, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        return orderMapper.toOrderResponseDTO(order);
    }

    /**
     * TÍNH NĂNG 1: Merchant bấm "Nhận đơn" (Chuyển trạng thái sang PREPARING)
     */
    @Override
    @Transactional
    public OrderResponseDTO acceptOrder(UUID merchantId, Long orderId) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể nhận đơn hàng khi đơn đang ở trạng thái 'Chờ nhận hàng'");
        }

        order.setStatus(OrderStatus.PREPARING);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Merchant {} đã nhận đơn hàng ID {}", merchantId, orderId);

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * TÍNH NĂNG 3: Merchant bấm "Hủy đơn" (Chuyển trạng thái sang CANCELLED)
     */
    @Override
    @Transactional
    public OrderResponseDTO cancelOrderByMerchant(UUID merchantId, Long orderId, String cancelReason) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng khi đơn đang ở trạng thái 'Chờ nhận hàng'");
        }

        String reason = (cancelReason != null && !cancelReason.trim().isEmpty())
                ? cancelReason.trim()
                : "Cửa hàng đã hủy đơn";

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Merchant {} đã hủy đơn hàng ID {}, lý do: {}", merchantId, orderId, reason);

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Merchant bấm "Đã nhận tiền & Hoàn thành" (Chuyển trạng thái sang COMPLETED)
     */
    @Override
    @Transactional
    public OrderResponseDTO completeOrder(UUID merchantId, Long orderId) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PREPARING && order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException("Chỉ có thể hoàn thành đơn hàng khi đơn đang chuẩn bị hoặc đang giao");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Merchant {} đã xác nhận hoàn thành đơn hàng ID {}", merchantId, orderId);

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Merchant bấm "Khách không nhận hàng" (Chuyển trạng thái sang CANCELLED)
     */
    @Override
    @Transactional
    public OrderResponseDTO markFailedDelivery(UUID merchantId, Long orderId, String cancelReason) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PREPARING && order.getStatus() != OrderStatus.DELIVERING && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể ghi nhận hủy đơn hàng ở trạng thái hiện tại");
        }

        String reason = (cancelReason != null && !cancelReason.trim().isEmpty())
                ? cancelReason.trim()
                : "Khách không nhận hàng";

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Merchant {} xác nhận khách không nhận đơn ID {}, lý do: {}", merchantId, orderId, reason);

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Đếm số lượng đơn hàng đang chờ Merchant tiếp nhận (PENDING)
     */
    @Override
    @Transactional(readOnly = true)
    public long countPendingOrders(UUID merchantId) {
        return orderRepository.countByMerchant_IdAndStatus(merchantId, OrderStatus.PENDING);
    }

    // ==================== HELPER METHOD ====================

    private Order findMerchantOrderOrThrow(Long orderId, UUID merchantId) {
        return orderRepository.findByIdAndMerchant_Id(orderId, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
    }
}
