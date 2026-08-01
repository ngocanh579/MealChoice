package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 500)
    private String fullAddress;

    @Column(nullable = false)
    private boolean defaultAddress = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    // Getter và Setter

    public Long getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
