package vn.codegyme.meal_choice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.codegyme.meal_choice.entity.Gender;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDTO {

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(min = 2, max = 30, message = "Tên hiển thị không được vượt quá 30 kí tự")
    private String displayName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDate dob;

    private Gender gender;

    private String avatarUrl;
}
