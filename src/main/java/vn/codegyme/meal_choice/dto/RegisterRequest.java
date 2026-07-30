package vn.codegyme.meal_choice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hơợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số"
    )
    private String password;

    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    private String confirmPassword;

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(max = 30, message = "Tên hiển thị không được vượt quá 30 kí tự")
    private String displayName;
}
