package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.service.AddressService;

@Controller
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Hiển thị danh sách địa chỉ của người đang đăng nhập.
     */
    @GetMapping
    public String showAddressList(
            Authentication authentication,
            Model model
    ) {
        String currentUserEmail = authentication.getName();

        model.addAttribute(
                "addresses",
                addressService.getAddressesByUserEmail(
                        currentUserEmail
                )
        );

        return "user/address-list";
    }

    /**
     * Xóa địa chỉ.
     */
    @PostMapping("/{addressId}/delete")
    public String deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String currentUserEmail = authentication.getName();

        try {
            addressService.deleteAddress(
                    addressId,
                    currentUserEmail
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa địa chỉ giao hàng thành công."
            );

        } catch (ResourceNotFoundException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/user/addresses";
    }
}
