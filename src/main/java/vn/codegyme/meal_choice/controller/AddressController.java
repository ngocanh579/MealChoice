package vn.codegyme.meal_choice.controller;

import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.service.AddressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(
            AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public String showAddressList(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        model.addAttribute(
                "addresses",
                addressService.getAddressesByUserEmail(email)
        );

        return "user/address-list";
    }

    @PostMapping("/{addressId}/delete")
    public String deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        try {
            addressService.deleteAddress(
                    addressId,
                    email
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xóa địa chỉ giao hàng."
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
