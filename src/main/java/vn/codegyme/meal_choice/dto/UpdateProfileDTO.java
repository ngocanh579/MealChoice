package vn.codegyme.meal_choice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.codegyme.meal_choice.entity.Gender;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDTO {

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 32, message = "Tên phải từ 2 đến 32 ký tự")
    private String displayName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dob;

    private Gender gender;

    private String avatarUrl;
}
