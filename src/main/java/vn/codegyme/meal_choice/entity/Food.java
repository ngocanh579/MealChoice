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
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_address_id", nullable = false)
    private MerchantAddress merchantAddress;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "food_category_mapping",
            joinColumns = @JoinColumn(name = "food_id"),
            inverseJoinColumns = @JoinColumn(name = "food_category_id")
    )
    @Builder.Default
    private List<FoodCategory> foodCategories = new ArrayList<>();

    @Column(name = "food_name", nullable = false, length = 150)
    private String foodName;

    @Column(name = "preparation_time")
    private Integer preparationTime;

    @Column(name = "food_note", columnDefinition = "TEXT")
    private String foodNote;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "service_fee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "is_recommended", nullable = false)
    @Builder.Default
    private Boolean isRecommended = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(
            mappedBy = "food",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<FoodImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (serviceFee == null) {
            serviceFee = BigDecimal.ZERO;
        }

        if (views == null) {
            views = 0;
        }

        if (orderCount == null) {
            orderCount = 0;
        }

        if (isRecommended == null) {
            isRecommended = false;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
