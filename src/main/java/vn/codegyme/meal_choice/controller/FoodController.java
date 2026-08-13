package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.codegyme.meal_choice.dto.food.FoodResponse;
import vn.codegyme.meal_choice.service.FoodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchant/{merchantId}/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponse>> getFoods(
            @PathVariable UUID merchantId,
            @RequestParam(required = false) String foodName) {

        if (foodName == null || foodName.trim().isEmpty()) {
            return ResponseEntity.ok(foodService.getFoods(merchantId));
        }

        return ResponseEntity.ok(foodService.searchFoods(merchantId, foodName));
    }
}
