package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.MerchantAddressResponse;
import vn.codegyme.meal_choice.dto.MerchantRegisterRequest;
import vn.codegyme.meal_choice.dto.MerchantResponse;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAddress;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerMerchant(
            MerchantRegisterRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy tài khoản người dùng"));

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "Mật khẩu nhập lại không đúng");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException(
                    "Mật khẩu tài khoản không đúng");
        }

        // Kiểm tra merchant cũ của user
        Merchant merchant = merchantRepository
                .findByUser_Id(user.getId())
                .orElse(null);

        if (merchant != null) {
            // Chỉ cho phép đăng ký lại nếu đang bị từ chối
            if (merchant.getMerchantStatus() != MerchantStatus.REJECTED) {
                throw new RuntimeException(
                        "Tài khoản này đã đăng ký Merchant");
            }

            // Kiểm tra trùng email/phone với merchant KHÁC (loại trừ chính mình)
            if (!merchant.getMerchantEmail().equals(request.getMerchantEmail())
                    && merchantRepository.existsByMerchantEmail(request.getMerchantEmail())) {
                throw new RuntimeException("Email Merchant đã tồn tại");
            }

            if (!merchant.getMerchantPhone().equals(request.getMerchantPhone())
                    && merchantRepository.existsByMerchantPhone(request.getMerchantPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại");
            }

            // Cập nhật lại thông tin merchant cũ
            merchant.setMerchantRestaurantName(request.getMerchantRestaurantName());
            merchant.setMerchantEmail(request.getMerchantEmail());
            merchant.setMerchantPhone(request.getMerchantPhone());
            merchant.setMerchantStatus(MerchantStatus.PENDING);
            merchant.setRejectReason(null); // Xóa lý do từ chối cũ
            merchantRepository.save(merchant);

            // Cập nhật địa chỉ (dùng địa chỉ đầu tiên nếu có, tạo mới nếu chưa có)
            List<MerchantAddress> addresses =
                    merchantAddressRepository.findByMerchantId(merchant.getId());

            MerchantAddress merchantAddress;
            if (!addresses.isEmpty()) {
                merchantAddress = addresses.get(0);
            } else {
                merchantAddress = new MerchantAddress();
                merchantAddress.setMerchant(merchant);
            }
            merchantAddress.setMerchantAddress(request.getMerchantAddress());
            merchantAddressRepository.save(merchantAddress);

        } else {
            // Đăng ký lần đầu — kiểm tra trùng email/phone
            if (merchantRepository.existsByMerchantEmail(request.getMerchantEmail())) {
                throw new RuntimeException("Email Merchant đã tồn tại");
            }

            if (merchantRepository.existsByMerchantPhone(request.getMerchantPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại");
            }

            // Tạo merchant mới
            merchant = new Merchant();
            merchant.setMerchantRestaurantName(request.getMerchantRestaurantName());
            merchant.setMerchantEmail(request.getMerchantEmail());
            merchant.setMerchantPhone(request.getMerchantPhone());
            merchant.setUser(user);
            merchant.setMerchantStatus(MerchantStatus.PENDING);
            merchantRepository.save(merchant);

            MerchantAddress merchantAddress = new MerchantAddress();
            merchantAddress.setMerchant(merchant);
            merchantAddress.setMerchantAddress(request.getMerchantAddress());
            merchantAddressRepository.save(merchantAddress);
        }

        emailService.sendMerchantRegisterEmail(
                request.getMerchantEmail(),
                request.getMerchantRestaurantName());
    }

    @Transactional
    public void updateMerchant(
            UUID merchantId,
            MerchantUpdateRequest request) {

        Merchant merchant = merchantRepository
                .findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Merchant"));

        merchant.setMerchantRestaurantName(
                request.getMerchantRestaurantName());

        merchantRepository.save(merchant);

        List<MerchantAddress> addresses =
                merchantAddressRepository
                        .findByMerchantId(merchantId);

        MerchantAddress address;

        if (!addresses.isEmpty()) {
            address = addresses.get(0);
        } else {
            address = new MerchantAddress();
            address.setMerchant(merchant);
        }

        address.setMerchantAddress(
                request.getMerchantAddress());

        address.setMerchantOpenTime(
                request.getMerchantOpenTime());

        address.setMerchantCloseTime(
                request.getMerchantCloseTime());

        merchantAddressRepository.save(address);
    }

    public MerchantResponse getMerchant(
            UUID merchantId) {

        Merchant merchant = merchantRepository
                .findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Merchant"));

        MerchantResponse response =
                new MerchantResponse();

        response.setId(merchant.getId());

        response.setMerchantRestaurantName(
                merchant.getMerchantRestaurantName());

        response.setMerchantEmail(
                merchant.getMerchantEmail());

        response.setMerchantPhone(
                merchant.getMerchantPhone());

        response.setMerchantStatus(
                merchant.getMerchantStatus());

        List<MerchantAddressResponse> addressResponses =
                merchantAddressRepository
                        .findByMerchantId(merchantId)
                        .stream()
                        .map(addr -> {

                            MerchantAddressResponse res =
                                    new MerchantAddressResponse();

                            res.setId(addr.getId());

                            res.setMerchantAddress(
                                    addr.getMerchantAddress());

                            res.setMerchantOpenTime(
                                    addr.getMerchantOpenTime());

                            res.setMerchantCloseTime(
                                    addr.getMerchantCloseTime());

                            return res;
                        })
                        .toList();

        response.setAddresses(addressResponses);

        return response;
    }
}