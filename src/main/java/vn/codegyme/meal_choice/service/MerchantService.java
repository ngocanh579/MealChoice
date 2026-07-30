package vn.codegyme.meal_choice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.MerchantRegisterRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.Role;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.RoleRepository;
import vn.codegyme.meal_choice.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class MerchantService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public void registerMerchant(MerchantRegisterRequest request){

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

        user.setName(request.getOwnerName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setPassword(request.getPassword());
        user.setIsActive(true);

        // lấy role_Merchant
        Role merchantRole = roleRepository
                .findByName(Role.RoleName.ROLE_MERCHANT)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy ROLE_MERCHANT"));
        user.getRoles().add(merchantRole);

        userRepository.save(user);

        // tạo Merchant
        Merchant merchant = new Merchant();

        merchant.setRestaurantName(request.getRestaurantName());
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());

        merchant.setUser(user);

        merchant.setMerchantStatus("PENDING");
        merchantRepository.save(merchant);
    }
    
}
