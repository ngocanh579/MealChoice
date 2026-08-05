package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne
    @JoinColumn(
            name = "merchant_id",
            nullable = false
    )
    private Merchant merchant;

    @Column(
            name = "merchant_address",
            nullable = false,
            length = 255
    )
    private String merchantAddress;

    @Column(name = "merchant_open_time")
    private LocalTime merchantOpenTime;

    @Column(name = "merchant_close_time")
    private LocalTime merchantCloseTime;
}