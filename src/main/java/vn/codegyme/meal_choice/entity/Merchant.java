package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(
            name = "merchant_restaurant_name",
            nullable = false,
            length = 150
    )
    private String merchantRestaurantName;

    @Column(
            name = "merchant_email",
            unique = true,
            nullable = false,
            length = 255
    )
    private String merchantEmail;

    @Column(
            name = "merchant_phone",
            unique = true,
            nullable = false,
            length = 20
    )
    private String merchantPhone;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "merchant_status",
            length = 30
    )
    private MerchantStatus merchantStatus;
    @Column(length = 500)
    private String lockReason;

    private LocalDateTime lockedAt;

    @Column(name = "is_trusted_partner", nullable = false)
    private boolean trustedPartner = false;

    @OneToOne
    @JoinColumn(
            name = "user_id",
            unique = true
    )
    private User user;

    @OneToMany(
            mappedBy = "merchant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MerchantAddress> addresses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "merchant_likes",
        joinColumns = @JoinColumn(name = "merchant_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();
}