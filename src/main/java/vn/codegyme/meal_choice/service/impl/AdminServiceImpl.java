package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.service.AdminService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;

    // Xem danh sách
    @Override
    @Transactional(readOnly = true)
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAllByOrderByIdDesc();
    }

    // Lọc theo trạng thái
    @Override
    @Transactional(readOnly = true)
    public List<Merchant> getMerchantsByStatus(MerchantStatus status) {
        return merchantRepository.findByMerchantStatusOrderByIdDesc(status);
    }

    // Xem chi tiết
    @Override
    @Transactional(readOnly = true)
    public Merchant getMerchantById(UUID id) {
        return findMerchant(id);
    }

    // Duyệt merchant
    @Override
    public void approveMerchant(UUID id) {
        Merchant merchant = findMerchant(id);

        merchant.setMerchantStatus(MerchantStatus.APPROVED);
        merchantRepository.save(merchant);
    }

    // Từ chối merchant
    @Override
    public void rejectMerchant(UUID id) {
        Merchant merchant = findMerchant(id);

        merchant.setMerchantStatus(MerchantStatus.REJECTED);
        merchant.setTrustedPartner(false);
        merchantRepository.save(merchant);
    }

    // Khóa / mở khóa
    @Override
    public void toggleMerchantLockStatus(UUID id) {
        Merchant merchant = findMerchant(id);

        if (merchant.getMerchantStatus() == MerchantStatus.BLOCKED) {
            merchant.setMerchantStatus(MerchantStatus.APPROVED);
        } else {
            merchant.setMerchantStatus(MerchantStatus.BLOCKED);
            merchant.setTrustedPartner(false);
        }

        merchantRepository.save(merchant);
    }

    // Duyệt đối tác thân thiết
    @Override
    public void approveTrustedPartner(UUID id) {
        Merchant merchant = findMerchant(id);

        if (merchant.getMerchantStatus() != MerchantStatus.APPROVED) {
            throw new IllegalStateException(
                    "Chỉ merchant đã được duyệt mới có thể trở thành đối tác thân thiết"
            );
        }

        merchant.setTrustedPartner(true);
        merchantRepository.save(merchant);
    }

    // Bỏ đối tác thân thiết
    @Override
    public void removeTrustedPartner(UUID id) {
        Merchant merchant = findMerchant(id);

        merchant.setTrustedPartner(false);
        merchantRepository.save(merchant);
    }

    private Merchant findMerchant(UUID id) {
        return merchantRepository.findByIdWithAddresses(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy merchant có id = " + id
                        )
                );
    }
}