package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.dto.MerchantDTO;

public interface AdminService {
    MerchantDTO getMerchantById(Long id);
    void toggleMerchantLockStatus(Long id);
}
