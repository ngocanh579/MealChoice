package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "merchant", "deliveryPartner", "items"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    // Mã đơn hàng ngắn gọn hiển thị cho user (VD: OD1A2B3C4D)
    @Column(nullable = false, unique = true, length = 20)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    // ===== Snapshot thông tin quán tại thời điểm đặt (đề phòng quán đổi tên/địa chỉ sau này) =====
    @Column(nullable = false, length = 150)
    private String merchantNameSnapshot;

    @Column(length = 255)
    private String merchantAddressSnapshot;

    // ===== Snapshot địa chỉ giao hàng (không tham chiếu FK Address để tránh vỡ lịch sử nếu user sửa/xóa địa chỉ) =====
    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 500)
    private String deliveryAddressSnapshot;

    // ===== Đối tác giao hàng =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(length = 150)
    private String deliveryPartnerNameSnapshot;

    @Column()
    private Double distanceKm;

    // ===== Tiền =====
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal; // Tổng tiền món (đã áp giá khuyến mãi nếu có)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal serviceFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // ===== Thanh toán =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    // ===== Trạng thái đơn =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 500)
    private String note;

    @Column(length = 500)
    private String cancelReason;

    @Column()
    private LocalDateTime cancelledAt;

    @Column()
    private LocalDateTime createdAt;

    @Column()
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.UNPAID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Đơn chỉ được phép hủy khi quán chưa bắt đầu chuẩn bị món.
     */
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
}
