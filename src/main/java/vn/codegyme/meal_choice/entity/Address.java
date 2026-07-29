package vn.codegyme.meal_choice.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== THÔNG TIN LIÊN HỆ ==========

    @Column(nullable = false)
    private String contactName; // Tên người nhận (*)

    @Column(nullable = false)
    private String contactPhone; // Số điện thoại người nhận (*)

    // ========== ĐỊA CHỈ ==========

    @Column(nullable = false)
    private String city; // Tỉnh/Thành phố (*)

    @Column(nullable = false)
    private String district; // Quận/Huyện (*)

    @Column(nullable = false)
    private String ward; // Phường/Xã (*)

    @Column(nullable = false)
    private String street; // Tên đường, Tòa nhà, Số nhà (*)

    private String note; // Ghi chú thêm

    @Column(nullable = false)
    private Boolean isDefault = false; // Địa chỉ mặc định

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= CONSTRUCTORS =================

    public Address() {
    }

    public Address(String contactName, String contactPhone, String city,
                   String district, String ward, String street) {
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.street = street;
        this.isDefault = false;
    }

    public Address(Long id, String contactName, String contactPhone,
                   String city, String district, String ward, String street,
                   String note, Boolean isDefault, User user) {
        this.id = id;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.street = street;
        this.note = note;
        this.isDefault = isDefault != null ? isDefault : false;
        this.user = user;
    }

    // ================= GETTERS AND SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ================= HELPER METHODS =================

    /**
     * Lấy địa chỉ đầy đủ dạng text
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", street, ward, district, city);
    }

    /**
     * Lấy thông tin liên hệ đầy đủ
     */
    public String getContactInfo() {
        return String.format("%s - %s", contactName, contactPhone);
    }

    // ================= EQUALS & HASHCODE =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", fullAddress='" + getFullAddress() + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}