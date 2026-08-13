package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "foods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @JoinColumn(name = "merchant_id", nullable = true)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_address_id", nullable = true)
    private MerchantAddress merchantAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    private FoodCategory category;

    @Builder.Default
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Size(max = 10, message = "Một món ăn chỉ có thể có tối đa 10 hình ảnh")
    private List<FoodImage> foodImages = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "food_likes",
            joinColumns = @JoinColumn(name = "food_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods / aliases
    public String getFoodName() {
        return name;
    }

    public void setFoodName(String foodName) {
        this.name = foodName;
    }

    public String getFoodNote() {
        return note;
    }

    public void setFoodNote(String foodNote) {
        this.note = foodNote;
    }

    public List<FoodImage> getImages() {
        return foodImages != null ? foodImages : new ArrayList<>();
    }

    public List<FoodCategory> getFoodCategories() {
        if (category != null) {
            return List.of(category);
        }
        return List.of();
    }

    public void setFoodCategories(List<FoodCategory> categories) {
        if (categories != null && !categories.isEmpty()) {
            this.category = categories.get(0);
        }
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price.doubleValue() : null;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice != null ? discountPrice.doubleValue() : null;
    }

    public void setDiscountPrice(Double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee != null ? serviceFee.doubleValue() : null;
    }

    public void setServiceFee(Double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getPriceAsBigDecimal() {
        return price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO;
    }

    public BigDecimal getDiscountPriceAsBigDecimal() {
        return discountPrice != null ? BigDecimal.valueOf(discountPrice) : null;
    }

    public BigDecimal getServiceFeeAsBigDecimal() {
        return serviceFee != null ? BigDecimal.valueOf(serviceFee) : BigDecimal.ZERO;
    }

    public Boolean isActive() {
        return active != null ? active : true;
    }

    public Boolean getIsActive() {
        return active != null ? active : true;
    }

    public void setIsActive(Boolean active) {
        this.active = active;
    }

    public String getAddress() {
        if (address != null && !address.isBlank()) {
            return address;
        }
        if (merchantAddress != null && merchantAddress.getMerchantAddress() != null) {
            return merchantAddress.getMerchantAddress();
        }
        return "";
    }

    // Trả về ảnh có ID bé nhất từ danh sách foodImages, nếu trống thì trả về imageUrl gốc
    public String getImageUrl() {
        if (foodImages != null && !foodImages.isEmpty()) {
            return foodImages.stream()
                    .filter(img -> img != null && img.getImageUrl() != null && !img.getImageUrl().isBlank())
                    .min(java.util.Comparator.comparing(img -> img.getId() != null ? img.getId().toString() : ""))
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
