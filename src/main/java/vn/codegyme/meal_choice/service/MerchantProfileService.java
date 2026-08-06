package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.MerchantProfileResponse;
import vn.codegyme.meal_choice.dto.MerchantUpdateRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAdminProfile;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.repository.MerchantAdminProfileRepository;
import vn.codegyme.meal_choice.repository.MerchantFeatureRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class MerchantProfileService {

    private final MerchantFeatureRepository merchantRepository;
    private final MerchantAdminProfileRepository adminProfileRepository;

    @Transactional(readOnly = true)
    public MerchantProfileResponse getCurrentMerchant() {
        return toResponse(findCurrentMerchant());
    }

    @Transactional
    public MerchantProfileResponse updateCurrentMerchant(MerchantUpdateRequest request) {
        Merchant merchant = findCurrentMerchant();
        merchant.setRestaurantName(request.getRestaurantName().trim());
        merchant.setAddress(request.getAddress().trim());
        merchant.setOpenTime(request.getOpenTime());
        merchant.setCloseTime(request.getCloseTime());
        return toResponse(merchantRepository.save(merchant));
    }

    private Merchant findCurrentMerchant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("Bạn cần đăng nhập bằng tài khoản merchant");
        }

        User user = userDetails.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản merchant đang bị khóa");
        }

        return merchantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ merchant"));
    }

    private MerchantProfileResponse toResponse(Merchant merchant) {
        boolean loyalPartner = adminProfileRepository.findByMerchantId(merchant.getId())
                .map(MerchantAdminProfile::isLoyalPartner)
                .orElse(false);
        User user = merchant.getUser();

        return new MerchantProfileResponse(
                merchant.getId(),
                user != null ? user.getDisplayName() : null,
                merchant.getRestaurantName(),
                merchant.getEmail(),
                merchant.getPhone(),
                merchant.getAddress(),
                merchant.getOpenTime(),
                merchant.getCloseTime(),
                merchant.getMerchantStatus(),
                loyalPartner,
                user != null && Boolean.TRUE.equals(user.getIsActive())
        );
    }
}
