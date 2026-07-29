package vn.codegyme.meal_choice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.AddressDTO;
import vn.codegyme.meal_choice.dto.UserDTO;
import vn.codegyme.meal_choice.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
//@PreAuthorize("hasRole('USER')") bỏ để test
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/user/profile - Lấy thông tin profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        UserDTO userDTO = userService.getCurrentUserProfile();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy thông tin profile thành công");
        response.put("data", userDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/user/profile - Cập nhật profile
     * KHÔNG cho phép sửa email và phoneNumber
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateProfile(userDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật profile thành công");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/user/address - Thêm địa chỉ mới
     */
    @PostMapping("/address")
    public ResponseEntity<Map<String, Object>> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        UserDTO updatedUser = userService.addAddress(addressDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thêm địa chỉ thành công");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/user/address/{id} - Sửa địa chỉ đã có
     */
    @PutMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {

        UserDTO updatedUser = userService.updateAddress(id, addressDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật địa chỉ thành công");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/user/address/{id} - Xóa địa chỉ
     */
    @DeleteMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Long id) {
        UserDTO updatedUser = userService.deleteAddress(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa địa chỉ thành công");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/user/address/{id}/set-default - Đặt địa chỉ mặc định
     */
    @PatchMapping("/address/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Long id) {
        UserDTO updatedUser = userService.setDefaultAddress(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đặt địa chỉ mặc định thành công");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }
}
