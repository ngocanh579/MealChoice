package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.dto.MerchantDTO;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.service.AdminService;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;

    private Merchant findMerchantById(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + id));
    }

    @Override
    public MerchantDTO getMerchantById(Long id) {
        Merchant merchant = findMerchantById(id);
        return MerchantDTO.fromEntity(merchant);
    }

    @Override
    public void toggleMerchantLockStatus(Long id) {
        Merchant merchant = findMerchantById(id);
        merchant.setActive(!merchant.isActive());
        merchantRepository.save(merchant);
    }
}
