package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "foods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "preparation_time")
    private Integer preparationTime;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private Double price;

    @Column(name = "discount_price")
    private Double discountPrice;

    @Column(name = "service_fee")
    private Double serviceFee;

    @Column(length = 100)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    private Merchant merchant;


    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public Boolean isActive() {
        return active != null ? active : true;
    }

    public Boolean getIsActive() {
        return active != null ? active : true;
    }

    public void setIsActive(Boolean active) {
        this.active = active;
    }

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "food_likes",
            joinColumns = @JoinColumn(name = "food_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Trả về ảnh có ID bé nhất từ danh sách foodImages, nếu trống thì trả về imageUrl gốc
    public String getImageUrl() {
        if (foodImages != null && !foodImages.isEmpty()) {
            return foodImages.stream()
                    .min(java.util.Comparator.comparing(FoodImage::getId))
                    .map(FoodImage::getImageUrl)
                    .orElse(imageUrl);
        }
        return imageUrl;
    }

    // Helper: Định dạng giá gốc (ví dụ: 65.000 đ) an toàn chống Exception
    public String getFormattedPrice() {
        try {
            if (price == null)
                return "0 đ";
            return String.format(java.util.Locale.US, "%,.0f đ", price).replace(',', '.');
        } catch (Exception e) {
            return "0 đ";
        }
    }

    // Helper: Định dạng giá khuyến mãi an toàn chống Exception
    public String getFormattedDiscountPrice() {
        try {
            if (discountPrice == null || discountPrice <= 0 || (price != null && discountPrice >= price)) {
                return null;
            }
            return String.format(java.util.Locale.US, "%,.0f đ", discountPrice).replace(',', '.');
        } catch (Exception e) {
            return null;
        }
    }

    // Helper: Tính % giảm giá (ví dụ: 30) an toàn chống Exception
    public Integer getDiscountPercentage() {
        try {
            if (price != null && discountPrice != null && price > 0 && discountPrice < price) {
                return (int) Math.round((price - discountPrice) * 100.0 / price);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // Helper: Lấy tên Category an toàn chống LazyInitializationException & Null
    public String getCategoryName() {
        try {
            if (category != null && category.getName() != null) {
                return category.getName();
            }
        } catch (Exception e) {
            return "Món ngon";
        }
        return "Món ngon";
    }

    // Helper: Mô tả ưu đãi/voucher động theo dữ liệu thực tế của món ăn trong CSDL
    public String getCouponDescription() {
        try {
            Integer discPercent = getDiscountPercentage();
            if (discPercent != null && discPercent > 0) {
                return "Freeship + Giảm " + discPercent + "%";
            }
            if (tag != null && !tag.isBlank()) {
                return "Freeship • " + tag;
            }
            return "Freeship đơn từ 0đ";
        } catch (Exception e) {
            return "Freeship Xtra";
        }
    }

}
