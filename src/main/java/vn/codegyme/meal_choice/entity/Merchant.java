package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    
    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
