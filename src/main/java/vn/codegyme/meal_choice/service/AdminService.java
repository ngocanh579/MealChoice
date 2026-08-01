package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.entity.Merchant;

public interface AdminService {
    Merchant getMerchantById(Long id);
    void toggleMerchantLockStatus(Long id);
}
