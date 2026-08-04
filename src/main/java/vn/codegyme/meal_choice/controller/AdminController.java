package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.codegyme.meal_choice.dto.MerchantDTO;
import vn.codegyme.meal_choice.service.AdminService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/merchants/{id}")
    public String viewMerchantDetail(@PathVariable Long id, Model model) {
        MerchantDTO merchant = adminService.getMerchantById(id);
        model.addAttribute("merchant", merchant);
        return "admin/merchant-detail";
    }

    @PostMapping("/merchants/{id}/toggle-lock")
    public String toggleMerchantLockStatus(@PathVariable Long id) {
        adminService.toggleMerchantLockStatus(id);
        return "redirect:/admin/merchants/" + id;
    }
}
