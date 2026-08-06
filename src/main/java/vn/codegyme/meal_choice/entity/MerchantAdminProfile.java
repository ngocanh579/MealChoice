package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_admin_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, unique = true)
    private Merchant merchant;

    @Column(nullable = false)
    private boolean loyalPartner = false;

    @Column(length = 30)
    private String statusBeforeLock;

    @Column(length = 500)
    private String reviewNote;

    @Column(length = 255)
    private String reviewedBy;

    private LocalDateTime reviewedAt;
}
