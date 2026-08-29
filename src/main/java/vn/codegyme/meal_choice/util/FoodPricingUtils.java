package vn.codegyme.meal_choice.util;

import vn.codegyme.meal_choice.entity.Food;
import vn.codegyme.meal_choice.entity.FoodImage;

import java.math.BigDecimal;

public final class FoodPricingUtils {

    private FoodPricingUtils() {
    }

    /**
     * Giá áp dụng hiện tại: ưu tiên discountPrice nếu có và nhỏ hơn giá gốc.
     */
    public static BigDecimal effectivePrice(Food food) {
        if (food.getDiscountPrice() != null && food.getDiscountPrice().compareTo(food.getPrice()) < 0) {
            return food.getDiscountPrice();
        }
        return food.getPrice();
    }

    public static String primaryImageUrl(Food food) {
        if (food.getImages() == null || food.getImages().isEmpty()) {
            return null;
        }
        return food.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(FoodImage::getImageUrl)
                .orElse(food.getImages().get(0).getImageUrl());
    }
}
