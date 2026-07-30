package vn.codegyme.meal_choice.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;


@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_name")
    private String restaurantName;

    private String email;
    private String phone;
    private String address;

    @Column(name ="open_time")
    private LocalTime openTime;

    @Column(name ="close_time")
    private LocalTime closeTime;

    @Column(name ="merchant_status")
    private String merchantStatus;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
