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
<<<<<<< HEAD
import vn.codegyme.meal_choice.entity.DeliveryPartner;
import vn.codegyme.meal_choice.entity.DeliveryPartnerStatus;
import vn.codegyme.meal_choice.repository.DeliveryPartnerRepository;
=======

>>>>>>> hung
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
<<<<<<< HEAD
    private final DeliveryPartnerRepository deliveryPartnerRepository;
=======
>>>>>>> hung
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
<<<<<<< HEAD
    @Transactional
    public void toggleLock(
            UUID id,
            String lockReason
    ) {

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đối tác vận chuyển."
                                )
                        );


        // ĐANG KHÓA -> MỞ
        if (partner.getStatus()
                == DeliveryPartnerStatus.BLOCKED) {

            partner.setStatus(
                    DeliveryPartnerStatus.ACTIVE
            );

            partner.setLockReason(null);
            partner.setLockedAt(null);
        }


        // ĐANG ACTIVE -> KHÓA
        else {

            if (lockReason == null
                    || lockReason.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập lý do khóa."
                );
            }

            partner.setStatus(
                    DeliveryPartnerStatus.BLOCKED
            );

            partner.setLockReason(
                    lockReason.trim()
            );

            partner.setLockedAt(
                    LocalDateTime.now()
            );
        }


        deliveryPartnerRepository.save(partner);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryPartner> getAllDeliveryPartners() {

        return deliveryPartnerRepository
                .findAllByOrderByCreatedAtDesc();
    }


    @Override
    @Transactional(readOnly = true)
    public List<DeliveryPartner> getDeliveryPartnersByStatus(
            DeliveryPartnerStatus status
    ) {

        return deliveryPartnerRepository
                .findByStatusOrderByCreatedAtDesc(status);
    }
    @Override
    @Transactional
    public void createDeliveryPartner(
            DeliveryPartner partner
    ) {

        // Kiểm tra mã đối tác đã tồn tại
        if (deliveryPartnerRepository
                .existsByPartnerCode(
                        partner.getPartnerCode()
                )) {

            throw new IllegalArgumentException(
                    "Mã đối tác vận chuyển đã tồn tại."
            );
        }


        // Đối tác do Admin tạo nên mặc định hoạt động
        partner.setStatus(
                DeliveryPartnerStatus.ACTIVE
        );


        // Chưa bị khóa
        partner.setLockReason(null);
        partner.setLockedAt(null);


        deliveryPartnerRepository.save(
                partner
        );
    }
    @Override
    @Transactional(readOnly = true)
    public DeliveryPartner getDeliveryPartnerById(UUID id) {

        return deliveryPartnerRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy đối tác vận chuyển có id = " + id
                        )
                );
    }
    // =====================================================
// CẬP NHẬT ĐỐI TÁC VẬN CHUYỂN
// =====================================================
    @Override
    @Transactional
    public void updateDeliveryPartner(
            UUID id,
            DeliveryPartner partner
    ) {

        DeliveryPartner existingPartner =
                deliveryPartnerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đối tác vận chuyển có id = " + id
                                )
                        );


        // ==============================
        // THÔNG TIN CƠ BẢN
        // ==============================

        existingPartner.setPartnerName(
                partner.getPartnerName()
        );

        existingPartner.setEmail(
                partner.getEmail()
        );

        existingPartner.setPhone(
                partner.getPhone()
        );

        existingPartner.setAddress(
                partner.getAddress()
        );

        existingPartner.setLogoUrl(
                partner.getLogoUrl()
        );


        // ==============================
        // CHÍNH SÁCH GIÁ
        // ==============================

        existingPartner.setBaseFee(
                partner.getBaseFee()
        );

        existingPartner.setBaseDistanceKm(
                partner.getBaseDistanceKm()
        );

        existingPartner.setFeePerKm(
                partner.getFeePerKm()
        );

        existingPartner.setPeakMultiplier(
                partner.getPeakMultiplier()
        );


        /*
         * Không update:
         *
         * partnerCode
         * status
         * lockReason
         * lockedAt
         * createdAt
         *
         * Các field này không nên bị thay đổi
         * từ form cập nhật thông tin thông thường.
         */


        deliveryPartnerRepository.save(
                existingPartner
        );
    }


    // =====================================================
// KHÓA / MỞ KHÓA ĐỐI TÁC VẬN CHUYỂN
// =====================================================
    @Override
    @Transactional
    public void toggleDeliveryPartnerLock(
            UUID id,
            String lockReason
    ) {

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đối tác vận chuyển có id = " + id
                                )
                        );


        // =================================================
        // ĐANG BỊ KHÓA -> MỞ KHÓA
        // =================================================

        if (partner.getStatus()
                == DeliveryPartnerStatus.BLOCKED) {

            partner.setStatus(
                    DeliveryPartnerStatus.ACTIVE
            );

            partner.setLockReason(null);

            partner.setLockedAt(null);
        }


        // =================================================
        // ĐANG HOẠT ĐỘNG -> KHÓA
        // =================================================

        else if (partner.getStatus()
                == DeliveryPartnerStatus.ACTIVE) {

            if (lockReason == null
                    || lockReason.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập lý do khóa đối tác vận chuyển."
                );
            }


            partner.setStatus(
                    DeliveryPartnerStatus.BLOCKED
            );

            partner.setLockReason(
                    lockReason.trim()
            );

            partner.setLockedAt(
                    LocalDateTime.now()
            );
        }


        // =================================================
        // TRẠNG THÁI KHÔNG HỢP LỆ
        // =================================================

        else {

            throw new IllegalStateException(
                    "Trạng thái đối tác vận chuyển không hợp lệ."
            );
        }


        deliveryPartnerRepository.save(
                partner
        );
    }
=======
>>>>>>> hung
}