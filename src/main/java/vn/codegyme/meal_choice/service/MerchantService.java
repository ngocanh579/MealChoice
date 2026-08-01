package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.MerchantRegisterRequest;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void registerMerchant(MerchantRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (userRepository.existsByPhoneNumber(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu nhập lại không đúng");
        }

        User user = new User();

        user.setDisplayName(request.getOwnerName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Tạm thời active tài khoản ngay.
        // TODO: Đổi thành false khi hoàn thiện chức năng kích hoạt qua email.
        user.setIsActive(true);

        // Lấy ROLE_MERCHANT
        Role merchantRole = roleRepository
                .findByName(Role.RoleName.ROLE_MERCHANT)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy ROLE_MERCHANT"));

        user.getRoles().add(merchantRole);

        userRepository.save(user);

        // Tạo Merchant
        Merchant merchant = new Merchant();

        merchant.setRestaurantName(request.getRestaurantName());
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());

        merchant.setUser(user);

        merchant.setMerchantStatus("PENDING");

        merchantRepository.save(merchant);

        emailService.sendMerchantRegisterEmail(
                request.getEmail(),
                request.getRestaurantName()
        );
    }

    public void updateMerchant(
            UUID merchantId,
            MerchantUpdateRequest request
    ) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));

        merchant.setRestaurantName(request.getRestaurantName());
        merchant.setAddress(request.getAddress());
        merchant.setOpenTime(request.getOpenTime());
        merchant.setCloseTime(request.getCloseTime());

        merchantRepository.save(merchant);
    }
}