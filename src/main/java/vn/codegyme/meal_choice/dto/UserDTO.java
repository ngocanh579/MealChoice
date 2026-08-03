package vn.codegyme.meal_choice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import vn.codegyme.meal_choice.entity.Gender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserDTO {

    private UUID id;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 32, message = "Tên phải từ 2 đến 32 ký tự")
    private String displayName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email; // Read-only sau khi tạo

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber; // Read-only sau khi tạo

    private String avatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dob;

    private Gender gender;

//    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    @Size(min = 1, message = "Phải có ít nhất một địa chỉ giao hàng")
    private List<AddressDTO> addresses;

    private Boolean isActive;

    // ================= CONSTRUCTORS =================

    public UserDTO() {
    }

    public UserDTO(UUID id, String displayName, String email, String phoneNumber,
                   String avatarUrl, LocalDateTime dob, Gender gender,
                   List<AddressDTO> addresses, Boolean isActive) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.dob = dob;
        this.gender = gender;
        this.addresses = addresses;
        this.isActive = isActive;
    }

    // ================= GETTERS AND SETTERS =================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}