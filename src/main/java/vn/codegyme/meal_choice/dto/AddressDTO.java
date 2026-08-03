package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {

    private Long id;

    // ========== THÔNG TIN LIÊN HỆ ==========

    @NotBlank(message = "Tên người nhận không được để trống")
    private String contactName; // Tên người nhận (*)

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (ví dụ: 0987654321)")
    private String contactPhone; // Số điện thoại người nhận (*)

    // ========== ĐỊA CHỈ ==========

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city; // Tỉnh/Thành phố (*)

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district; // Quận/Huyện (*)

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward; // Phường/Xã (*)

    @NotBlank(message = "Tên đường, Tòa nhà, Số nhà không được để trống")
    private String street; // Tên đường, Tòa nhà, Số nhà (*)

    private String note; // Ghi chú

    private Boolean isDefault; // Địa chỉ mặc định

    // ================= CONSTRUCTORS =================

    public AddressDTO() {
    }

    public AddressDTO(Long id, String contactName, String contactPhone,
                      String city, String district, String ward, String street,
                      String note, Boolean isDefault) {
        this.id = id;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.street = street;
        this.note = note;
        this.isDefault = isDefault;
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
}
