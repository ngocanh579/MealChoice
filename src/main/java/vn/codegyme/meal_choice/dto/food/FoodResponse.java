package vn.codegyme.meal_choice.dto.food;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FoodResponse {

    private Long id;

    private UUID merchantAddressId;

    private String merchantAddress;

    private String foodName;

    private Integer preparationTime;

    private String foodNote;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private BigDecimal serviceFee;

    private Integer views;

    private Integer orderCount;

    private Boolean isRecommended;

    private List<Long> categoryIds;

    private List<String> categoryNames;

    private List<Long> tagIds;

    private List<String> tagNames;

    private List<String> imageUrls;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}