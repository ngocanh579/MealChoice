package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(
            name = "restaurant_name",
            nullable = false,
            length = 150
    )
    private String restaurantName;

    @Column(
            name = "merchant_email",
            unique = true,
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            name = "merchant_phone",
            unique = true,
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(
            name = "merchant_address",
            nullable = false,
            length = 255
    )
    private String address;

    @Column(name = "merchant_open_time")
    private LocalTime openTime;

    @Column(name = "merchant_close_time")
    private LocalTime closeTime;

    @Column(
            name = "merchant_status",
            length = 30
    )
    private String merchantStatus;

    @OneToOne
    @JoinColumn(
            name = "user_id",
            unique = true
    )
    private User user;
}