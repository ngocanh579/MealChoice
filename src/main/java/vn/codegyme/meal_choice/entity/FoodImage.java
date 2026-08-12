package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "food")
public class FoodImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    public FoodImage(String imageUrl, Food food) {
        this.imageUrl = imageUrl;
        this.food = food;
    }
}
