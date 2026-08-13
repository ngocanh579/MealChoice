package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.AdminService;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

        User user = merchant.getUser();

        if (user == null) {
            throw new IllegalArgumentException(
                    "Merchant chưa liên kết với tài khoản User"
            );
        }

        Role merchantRole = roleRepository
                .findByName(Role.RoleName.ROLE_MERCHANT)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy role ROLE_MERCHANT"
                        )
                );

        user.getRoles().add(merchantRole);

        System.out.println("USER: " + user.getEmail());
        System.out.println("ROLES TRƯỚC SAVE: " + user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList());
        userRepository.save(user);
        merchantRepository.save(merchant);
    }

    // Từ chối merchant
    @Override
    public void rejectMerchant(UUID id, String reason) {
        Merchant merchant = findMerchant(id);

        merchant.setMerchantStatus(MerchantStatus.REJECTED);
        merchant.setTrustedPartner(false);
        merchant.setRejectReason(reason);
        merchantRepository.save(merchant);
    }

    // Khóa /lý do khóa/ mở khóa
    @Override
    @Transactional
    public void toggleMerchantLockStatus(
            UUID id,
            String lockReason
    ) {

        Merchant merchant = merchantRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy merchant."
                        )
                );

        // Merchant đang bị khóa -> mở khóa
        if (merchant.getMerchantStatus() == MerchantStatus.BLOCKED) {

            merchant.setMerchantStatus(MerchantStatus.APPROVED);

            merchant.setLockReason(null);
            merchant.setLockedAt(null);

        } else {

            // Merchant đang hoạt động -> khóa
            if (lockReason == null
                    || lockReason.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập lý do khóa merchant."
                );
            }

            merchant.setMerchantStatus(MerchantStatus.BLOCKED);

            merchant.setLockReason(
                    lockReason.trim()
            );

            merchant.setLockedAt(
                    LocalDateTime.now()
            );
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