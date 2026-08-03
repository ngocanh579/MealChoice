package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "password", "roles", "addresses" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // Ép Hibernate lưu UUID thành kiểu VARCHAR(36)
    @Column(length = 36)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false, unique = true, updatable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true") // Sinh ra SQL DDL: DEFAULT true
    private Boolean isActive = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Address> addresses = new HashSet<>();

    public User(String name, String email, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isActive = true;
        this.roles = new HashSet<>();
        this.addresses = new HashSet<>();
    }

    // Email và Phone không được sửa sau khi đã tạo
    public void setEmail(String email) {
        if (this.id == null) {
            this.email = email;
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this.id == null) {
            this.phoneNumber = phoneNumber;
        }
    }
}