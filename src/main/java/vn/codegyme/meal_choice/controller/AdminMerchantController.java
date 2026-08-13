package vn.codegyme.meal_choice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.service.AdminService;

import java.util.UUID;

@Controller
@RequestMapping("/admin/merchants")
public class AdminMerchantController {

    private final AdminService adminService;

    public AdminMerchantController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Danh sách merchant
    @GetMapping
    public String showMerchantList(@RequestParam(required = false) MerchantStatus status, Model model) {

        model.addAttribute("merchants", status == null ? adminService.getAllMerchants() : adminService.getMerchantsByStatus(status));

        model.addAttribute("selectedStatus", status);

        return "admin/merchant/list";
    }

    // Xem chi tiết merchant
    @GetMapping("/{id}")
    public String showMerchantDetail(@PathVariable UUID id, Model model) {

        model.addAttribute("merchant", adminService.getMerchantById(id));

        return "admin/merchant/detail";
    }

    // Duyệt
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable UUID id, RedirectAttributes redirectAttributes) {

        adminService.approveMerchant(id);
        redirectAttributes.addFlashAttribute("message", "Đã duyệt đăng ký merchant.");

        return "redirect:/admin/merchants";
    }

    // Từ chối
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        adminService.rejectMerchant(id, rejectReason);
        redirectAttributes.addFlashAttribute("message", "Đã từ chối đăng ký merchant.");

        return "redirect:/admin/merchants";
    }

    // Khóa / mở khóa
    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable UUID id, RedirectAttributes redirectAttributes) {

        adminService.toggleMerchantLockStatus(id);
        redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái khóa merchant.");

        return "redirect:/admin/merchants";
    }

    // Duyệt đối tác thân thiết
    @PostMapping("/{id}/trusted-partner")
    public String approveTrustedPartner(@PathVariable UUID id, RedirectAttributes redirectAttributes) {

        try {
            adminService.approveTrustedPartner(id);
            redirectAttributes.addFlashAttribute("message", "Đã duyệt đối tác thân thiết.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/merchants";
    }

    // Bỏ đối tác thân thiết
    @PostMapping("/{id}/trusted-partner/remove")
    public String removeTrustedPartner(@PathVariable UUID id, RedirectAttributes redirectAttributes) {

        adminService.removeTrustedPartner(id);
        redirectAttributes.addFlashAttribute("message", "Đã bỏ trạng thái đối tác thân thiết.");

        return "redirect:/admin/merchants";
    }
}