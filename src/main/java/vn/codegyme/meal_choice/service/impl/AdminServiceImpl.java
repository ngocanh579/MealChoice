package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.*;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.TrustedPartnerRequestRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.AdminService;
import vn.codegyme.meal_choice.service.EmailService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TrustedPartnerRequestRepository trustedPartnerRequestRepository;
    private final EmailService emailService;
    // Xem danh sách
    @Override
    @Transactional(readOnly = true)
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAllWithAddressesOrderByIdDesc();
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

        TrustedPartnerRequest request =
                trustedPartnerRequestRepository
                        .findFirstByMerchant_IdAndStatusOrderByCreatedAtDesc(
                                id,
                                TrustedPartnerRequestStatus.PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Không tìm thấy yêu cầu đăng ký đối tác thân thiết đang chờ duyệt"
                                ));

        request.setStatus(TrustedPartnerRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());

        merchant.setTrustedPartner(true);

        trustedPartnerRequestRepository.save(request);
        merchantRepository.save(merchant);

        emailService.sendTrustedPartnerApprovedEmail(
                merchant.getMerchantEmail(),
                merchant.getMerchantRestaurantName()
        );
    }

    // từ chối đối tác thân thiết
    @Override
    public void rejectTrustedPartner(UUID id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Vui lòng nhập lý do từ chối"
            );
        }

        Merchant merchant = findMerchant(id);

        TrustedPartnerRequest request =
                trustedPartnerRequestRepository
                        .findFirstByMerchant_IdAndStatusOrderByCreatedAtDesc(
                                id,
                                TrustedPartnerRequestStatus.PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Không tìm thấy yêu cầu đăng ký đối tác thân thiết đang chờ duyệt"
                                ));

        request.setStatus(TrustedPartnerRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectReason(reason.trim());

        trustedPartnerRequestRepository.save(request);

        emailService.sendTrustedPartnerRejectedEmail(
                merchant.getMerchantEmail(),
                merchant.getMerchantRestaurantName(),
                reason.trim()
        );
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

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> getPendingTrustedPartnerMerchantIds() {
        return trustedPartnerRequestRepository
                .findByStatusOrderByCreatedAtDesc(
                        TrustedPartnerRequestStatus.PENDING
                )
                .stream()
                .map(request -> request.getMerchant().getId())
                .collect(Collectors.toSet());
    }
}