package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.service.AdminService;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;

    @Override
    public Merchant getMerchantById(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + id));
    }

    @Override
    public void toggleMerchantLockStatus(Long id) {
        Merchant merchant = getMerchantById(id);
        merchant.setActive(!merchant.isActive());
        merchantRepository.save(merchant);
    }
}
