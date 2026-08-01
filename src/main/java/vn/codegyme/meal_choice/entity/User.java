package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(
            name = "user_email",
            unique = true
    )
    private String email;

    @Column(
            name = "user_password",
            nullable = false,
            length = 60
    )
    private String password;

    @Column(
            name = "user_display_name",
            nullable = false,
            length = 32
    )
    private String displayName;

    @Column(
            name = "user_phone_number",
            unique = true,
            nullable = false,
            length = 20
    )
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "user_gender",
            length = 20
    )
    private Gender gender;

    @Column(name = "user_avatar_url")
    private String avatarUrl;

    @Column(name = "user_dob")
    private LocalDateTime dob;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Address> addresses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(
            name = "user_is_active",
            nullable = false
    )
    private Boolean isActive = true;

    @Column(
            name = "user_created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}