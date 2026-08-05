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
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantAddressRepository;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final MerchantRepository merchantRepository;
        private final MerchantAddressRepository merchantAddressRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        // Đăng ký Merchant (đã fix user không thể đăng kí merchant cho user khác)
        @Transactional
        public void registerMerchant(MerchantRegisterRequest request) {

                if (userRepository.existsByEmail(request.getMerchantEmail())
                                || merchantRepository.existsByMerchantEmail(request.getMerchantEmail())) {
                        throw new RuntimeException("Email đã tồn tại");
                }

                if (userRepository.existsByPhoneNumber(request.getMerchantPhone())
                                || merchantRepository.existsByMerchantPhone(request.getMerchantPhone())) {
                        throw new RuntimeException("Số điện thoại đã tồn tại");
                }

                if (!request.getPassword().equals(request.getConfirmPassword())) {
                        throw new RuntimeException("Mật khẩu nhập lại không đúng");
                }

                // Tạo User
                User user = new User();

                user.setDisplayName(request.getOwnerName());
                user.setEmail(request.getMerchantEmail());
                user.setPhoneNumber(request.getMerchantPhone());
                user.setPassword(
                                passwordEncoder.encode(request.getPassword()));

                user.setIsActive(true);

                // Lấy ROLE_MERCHANT
                Role merchantRole = roleRepository
                                .findByName(Role.RoleName.ROLE_MERCHANT)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_MERCHANT"));

                user.getRoles().add(merchantRole);

                userRepository.save(user);

                // Tạo Merchant
                Merchant merchant = new Merchant();

                merchant.setMerchantRestaurantName(
                                request.getMerchantRestaurantName());

                merchant.setMerchantEmail(
                                request.getMerchantEmail());

                merchant.setMerchantPhone(
                                request.getMerchantPhone());

                merchant.setUser(user);
                merchant.setMerchantStatus("PENDING");

                merchantRepository.save(merchant);

                // Tạo địa chỉ đầu tiên cho Merchant
                MerchantAddress merchantAddress = new MerchantAddress();

                merchantAddress.setMerchant(merchant);
                merchantAddress.setMerchantAddress(
                                request.getMerchantAddress());

                merchantAddressRepository.save(merchantAddress);

                // Gửi email thông báo
                emailService.sendMerchantRegisterEmail(
                                request.getMerchantEmail(),
                                request.getMerchantRestaurantName());
        }

        // Cập nhật thông tin Merchant
        @Transactional
        public void updateMerchant(UUID merchantId, MerchantUpdateRequest request) {

                Merchant merchant = merchantRepository.findById(merchantId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Merchant"));

                // Cập nhật tên nhà hàng
                merchant.setMerchantRestaurantName(
                                request.getMerchantRestaurantName());

                merchantRepository.save(merchant);

                // Cập nhật địa chỉ của Merchant
                List<MerchantAddress> addresses = merchantAddressRepository.findByMerchantId(merchantId);
                if (!addresses.isEmpty()) {
                        MerchantAddress address = addresses.get(0);
                        address.setMerchantAddress(request.getMerchantAddress());
                        address.setMerchantOpenTime(request.getMerchantOpenTime());
                        address.setMerchantCloseTime(request.getMerchantCloseTime());
                        merchantAddressRepository.save(address);
                }
        }

        // Lấy thông tin Merchant
        public MerchantResponse getMerchant(UUID merchantId) {

                Merchant merchant = merchantRepository.findById(merchantId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Merchant"));

                MerchantResponse response = new MerchantResponse();

                response.setId(merchant.getId());

                response.setMerchantRestaurantName(
                                merchant.getMerchantRestaurantName());

                response.setMerchantEmail(
                                merchant.getMerchantEmail());

                response.setMerchantPhone(
                                merchant.getMerchantPhone());

                response.setMerchantStatus(
                                merchant.getMerchantStatus());

                // Map danh sách địa chỉ của Merchant
                List<MerchantAddressResponse> addressResponses = merchantAddressRepository
                                .findByMerchantId(merchantId)
                                .stream()
                                .map(addr -> {
                                        MerchantAddressResponse res = new MerchantAddressResponse();
                                        res.setId(addr.getId());
                                        res.setMerchantAddress(addr.getMerchantAddress());
                                        res.setMerchantOpenTime(addr.getMerchantOpenTime());
                                        res.setMerchantCloseTime(addr.getMerchantCloseTime());
                                        return res;
                                })
                                .toList();

                response.setAddresses(addressResponses);

                return response;
        }
}