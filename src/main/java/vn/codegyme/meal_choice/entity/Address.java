package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name="addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column()
    private String label; // Nhà, công ty

    @Column(nullable = false)
    private String receiptName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String ward;

//    @Column(nullable = false)
//    private String fullAddress; // Địa chỉ lấy từ bản đồ

    @Column(nullable = false)
    private String buildingDetails; // Tên đường, Tòa nhà, Số nhà (*)

//    // Tọa độ GPS - Dùng để tính khoảng cách & Phí Ship
//    @Column(nullable = false)
//    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 200)
    private String deliveryNote;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
