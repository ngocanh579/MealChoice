package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    // Xem danh sách merchant
    List<Merchant> getAllMerchants();

    // Lọc theo trạng thái
    List<Merchant> getMerchantsByStatus(MerchantStatus status);

    // Xem chi tiết
    Merchant getMerchantById(UUID id);

    // Duyệt merchant
    void approveMerchant(UUID id);

    // Từ chối merchant
    void rejectMerchant(UUID id, String reason);

    // Khóa/ lý do khóa/ mở khóa
    void toggleMerchantLockStatus(
            UUID id,
            String lockReason
    );

    // Duyệt đối tác thân thiết
    void approveTrustedPartner(UUID id);

    // Bỏ đối tác thân thiết
    void removeTrustedPartner(UUID id);
}