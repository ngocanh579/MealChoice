package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.Delivery.GeoPoint;
import vn.codegyme.meal_choice.dto.order.OrderResponseDTO;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.entity.Order;
import vn.codegyme.meal_choice.entity.OrderStatus;
import vn.codegyme.meal_choice.mapper.OrderMapper;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.OrderRepository;
import vn.codegyme.meal_choice.service.DistanceService;
import vn.codegyme.meal_choice.service.GeocodingService;
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
    private final MerchantAddressRepository merchantAddressRepository;
    private final GeocodingService geocodingService;
    private final DistanceService distanceService;

    /**
     * TÍNH NĂNG 1: Lấy danh sách đơn hàng của Merchant (có thể lọc theo trạng thái)
     */
    @Override
    @Transactional
    public List<OrderResponseDTO> getMerchantOrders(UUID merchantId, OrderStatus status) {
        List<Order> orders;

        if (status != null) {
            orders = orderRepository.findByMerchant_IdAndStatusOrderByIdDesc(merchantId, status);
        } else {
            orders = orderRepository.findByMerchant_IdOrderByIdDesc(merchantId);
        }

        for (Order order : orders) {
            autoSyncOrderStatus(order);
        }

        return orderMapper.toOrderResponseDTOList(orders);
    }

    /**
     * TÍNH NĂNG 1: Lấy danh sách đơn hàng của Merchant có phân trang
     */
    @Override
    @Transactional
    public Page<OrderResponseDTO> getMerchantOrders(UUID merchantId, OrderStatus status, Pageable pageable) {
        Page<Order> orderPage;

        if (status != null) {
            orderPage = orderRepository.findByMerchant_IdAndStatusOrderByIdDesc(merchantId, status, pageable);
        } else {
            orderPage = orderRepository.findByMerchant_IdOrderByIdDesc(merchantId, pageable);
        }

        for (Order order : orderPage.getContent()) {
            autoSyncOrderStatus(order);
        }

        return orderMapper.toOrderResponseDTOPage(orderPage);
    }

    /**
     * TÍNH NĂNG 2: Xem chi tiết một đơn hàng của Merchant
     */
    @Override
    @Transactional
    public OrderResponseDTO getMerchantOrderDetail(UUID merchantId, Long orderId) {
        Order order = orderRepository.findByIdAndMerchantIdWithItems(orderId, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        autoSyncOrderStatus(order);

        return orderMapper.toOrderResponseDTO(order);
    }

    /**
     * TÍNH NĂNG 1: Merchant bấm "Nhận đơn"
     */
    @Override
    @Transactional
    public OrderResponseDTO acceptOrder(UUID merchantId, Long orderId) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể nhận đơn hàng khi đơn đang ở trạng thái 'Chờ nhận hàng'");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.PREPARING);
        order.setAcceptedAt(now);

        int prepMinutes = 10;

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            int maxPrep = order.getOrderItems().stream()
                    .mapToInt(item -> (
                            item.getFood() != null
                                    && item.getFood().getPreparationTime() != null
                                    && item.getFood().getPreparationTime() > 0
                    )
                            ? item.getFood().getPreparationTime()
                            : 10)
                    .max()
                    .orElse(10);

            prepMinutes = Math.max(5, maxPrep);
        }

        LocalDateTime preparingUntil = now.plusMinutes(prepMinutes);
        order.setPreparingUntil(preparingUntil);

        int transitMinutes = calculateDeliveryTransitMinutes(order);
        order.setEstimatedDeliveryTime(preparingUntil.plusMinutes(transitMinutes));
        order.setUpdatedAt(now);

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Merchant {} đã nhận đơn hàng ID {}, chuẩn bị đến {}, dự kiến giao {}",
                merchantId,
                orderId,
                preparingUntil,
                order.getEstimatedDeliveryTime()
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Merchant bấm "Bắt đầu giao hàng"
     */
    @Override
    @Transactional
    public OrderResponseDTO startDelivery(UUID merchantId, Long orderId) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Chỉ có thể bắt đầu giao hàng khi đơn đang ở trạng thái 'Đang chuẩn bị'"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        order.setStatus(OrderStatus.DELIVERING);

        int transitMinutes = calculateDeliveryTransitMinutes(order);
        order.setEstimatedDeliveryTime(now.plusMinutes(transitMinutes));
        order.setUpdatedAt(now);

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Merchant {} đã bắt đầu giao đơn hàng ID {}, dự kiến giao {}",
                merchantId,
                orderId,
                order.getEstimatedDeliveryTime()
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * TÍNH NĂNG 3: Merchant bấm "Hủy đơn"
     */
    @Override
    @Transactional
    public OrderResponseDTO cancelOrderByMerchant(UUID merchantId, Long orderId, String cancelReason) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy đơn hàng khi đơn đang ở trạng thái 'Chờ nhận hàng'"
            );
        }

        String reason = cancelReason != null && !cancelReason.trim().isEmpty()
                ? cancelReason.trim()
                : "Cửa hàng đã hủy đơn";

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Merchant {} đã hủy đơn hàng ID {}, lý do: {}",
                merchantId,
                orderId,
                reason
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Merchant bấm "Đã nhận tiền & Hoàn thành"
     */
    @Override
    @Transactional
    public OrderResponseDTO completeOrder(UUID merchantId, Long orderId) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        autoSyncOrderStatus(order);

        if (order.getStatus() != OrderStatus.PREPARING
                && order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException(
                    "Chỉ có thể hoàn thành đơn hàng khi đơn đang chuẩn bị hoặc đang giao"
            );
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Merchant {} đã xác nhận hoàn thành đơn hàng ID {}",
                merchantId,
                orderId
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Merchant bấm "Khách không nhận hàng"
     */
    @Override
    @Transactional
    public OrderResponseDTO markFailedDelivery(UUID merchantId, Long orderId, String cancelReason) {
        Order order = findMerchantOrderOrThrow(orderId, merchantId);

        if (order.getStatus() != OrderStatus.PREPARING
                && order.getStatus() != OrderStatus.DELIVERING
                && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Không thể ghi nhận hủy đơn hàng ở trạng thái hiện tại"
            );
        }

        String reason = cancelReason != null && !cancelReason.trim().isEmpty()
                ? cancelReason.trim()
                : "Khách không nhận hàng";

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Merchant {} xác nhận khách không nhận đơn ID {}, lý do: {}",
                merchantId,
                orderId,
                reason
        );

        return orderMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Đếm số lượng đơn hàng đang chờ Merchant tiếp nhận
     */
    @Override
    @Transactional(readOnly = true)
    public long countPendingOrders(UUID merchantId) {
        return orderRepository.countByMerchant_IdAndStatus(
                merchantId,
                OrderStatus.PENDING
        );
    }

    private void autoSyncOrderStatus(Order order) {
        if (order == null) {
            return;
        }

        if (order.getStatus() == OrderStatus.PREPARING
                && order.getPreparingUntil() != null
                && LocalDateTime.now().isAfter(order.getPreparingUntil())) {

            order.setStatus(OrderStatus.DELIVERING);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            log.info(
                    "Tự động chuyển đơn hàng ID {} sang DELIVERING do hết thời gian chuẩn bị",
                    order.getId()
            );
        }
    }

    private Order findMerchantOrderOrThrow(Long orderId, UUID merchantId) {
        return orderRepository.findByIdAndMerchant_Id(orderId, merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId)
                );
    }

    /**
     * Tính thời gian vận chuyển giao hàng dự kiến = 4 phút / 1km
     */
    private int calculateDeliveryTransitMinutes(Order order) {
        double distanceKm = 3.0;

        try {
            if (order != null
                    && order.getMerchant() != null
                    && order.getDeliveryAddress() != null) {

                List<MerchantAddress> merchantAddresses =
                        merchantAddressRepository.findByMerchantId(
                                order.getMerchant().getId()
                        );

                if (!merchantAddresses.isEmpty()) {
                    MerchantAddress merchantAddress = merchantAddresses.get(0);

                    GeoPoint merchantPoint;

                    if (merchantAddress.getLatitude() != null
                            && merchantAddress.getLongitude() != null) {
                        merchantPoint = new GeoPoint(
                                merchantAddress.getLatitude(),
                                merchantAddress.getLongitude()
                        );
                    } else {
                        merchantPoint = geocodingService.geocode(
                                merchantAddress.getMerchantAddress() + ", Việt Nam"
                        );
                    }

                    GeoPoint userPoint = geocodingService.geocode(
                            order.getDeliveryAddress() + ", Việt Nam"
                    );

                    if (merchantPoint != null && userPoint != null) {
                        double calculatedDistance =
                                distanceService.calculateDistanceKm(
                                        merchantPoint,
                                        userPoint
                                );

                        if (order.getDeliveryAddress().toLowerCase().contains("hà nội")
                                && merchantAddress.getMerchantAddress()
                                .toLowerCase()
                                .contains("hà nội")
                                && calculatedDistance > 35.0) {
                            calculatedDistance = 3.0;
                        }

                        if (calculatedDistance > 0) {
                            distanceKm = calculatedDistance;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn(
                    "Không tính được khoảng cách đơn hàng ID {}, dùng mặc định 3km: {}",
                    order != null ? order.getId() : null,
                    e.getMessage()
            );
        }

        return (int) Math.max(4, Math.round(distanceKm * 4));
    }
}