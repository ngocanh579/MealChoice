package vn.codegyme.meal_choice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.AdminMerchantResponse;
import vn.codegyme.meal_choice.dto.MerchantDecisionRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAdminProfile;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.exception.ResourceNotFoundException;
import vn.codegyme.meal_choice.repository.MerchantAdminProfileRepository;
import vn.codegyme.meal_choice.repository.MerchantFeatureRepository;
import vn.codegyme.meal_choice.repository.RefreshTokenRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMerchantManagementService {

    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String BLOCKED = "BLOCKED";

    private final MerchantFeatureRepository merchantRepository;
    private final MerchantAdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public List<AdminMerchantResponse> findAll(String status, String keyword) {
        String normalizedStatus = normalize(status);
        String normalizedKeyword = normalize(keyword);

        return merchantRepository.findAll().stream()
                .filter(merchant -> normalizedStatus == null || normalizedStatus.equals(normalize(merchant.getMerchantStatus())))
                .filter(merchant -> matchesKeyword(merchant, normalizedKeyword))
                .sorted(Comparator.comparing(Merchant::getRestaurantName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminMerchantResponse findById(UUID merchantId) {
        return toResponse(findMerchant(merchantId));
    }

    @Transactional
    public AdminMerchantResponse decide(UUID merchantId, MerchantDecisionRequest request) {
        Merchant merchant = findMerchant(merchantId);
        if (BLOCKED.equalsIgnoreCase(merchant.getMerchantStatus())) {
            throw new RuntimeException("Hãy mở khóa merchant trước khi duyệt hoặc từ chối");
        }

        String decision = request.decision().toUpperCase(Locale.ROOT);
        String reason = trimToNull(request.reason());
        if ("REJECT".equals(decision) && reason == null) {
            throw new RuntimeException("Vui lòng nhập lý do từ chối merchant");
        }

        MerchantAdminProfile profile = getOrCreateProfile(merchant);
        profile.setReviewNote(reason);
        profile.setReviewedBy(currentReviewer());
        profile.setReviewedAt(LocalDateTime.now());

        User user = merchant.getUser();
        if ("APPROVE".equals(decision)) {
            merchant.setMerchantStatus(APPROVED);
            if (user != null) {
                user.setIsActive(true);
            }
        } else {
            merchant.setMerchantStatus(REJECTED);
            profile.setLoyalPartner(false);
            if (user != null) {
                user.setIsActive(false);
                refreshTokenRepository.deleteByUser(user);
            }
        }

        saveMerchantAndUser(merchant);
        adminProfileRepository.save(profile);
        return toResponse(merchant);
    }

    @Transactional
    public AdminMerchantResponse setLocked(UUID merchantId, boolean locked) {
        Merchant merchant = findMerchant(merchantId);
        MerchantAdminProfile profile = getOrCreateProfile(merchant);
        User user = merchant.getUser();

        if (locked) {
            if (!BLOCKED.equalsIgnoreCase(merchant.getMerchantStatus())) {
                profile.setStatusBeforeLock(defaultStatus(merchant.getMerchantStatus()));
            }
            merchant.setMerchantStatus(BLOCKED);
            if (user != null) {
                user.setIsActive(false);
                refreshTokenRepository.deleteByUser(user);
            }
        } else if (BLOCKED.equalsIgnoreCase(merchant.getMerchantStatus())) {
            String restoredStatus = defaultStatus(profile.getStatusBeforeLock());
            merchant.setMerchantStatus(restoredStatus);
            if (user != null) {
                user.setIsActive(!REJECTED.equals(restoredStatus));
            }
            profile.setStatusBeforeLock(null);
        }

        profile.setReviewedBy(currentReviewer());
        profile.setReviewedAt(LocalDateTime.now());
        saveMerchantAndUser(merchant);
        adminProfileRepository.save(profile);
        return toResponse(merchant);
    }

    @Transactional
    public AdminMerchantResponse setLoyalPartner(UUID merchantId, boolean approved) {
        Merchant merchant = findMerchant(merchantId);
        if (approved && !APPROVED.equalsIgnoreCase(merchant.getMerchantStatus())) {
            throw new RuntimeException("Chỉ merchant đã được duyệt mới có thể trở thành đối tác thân thiết");
        }

        MerchantAdminProfile profile = getOrCreateProfile(merchant);
        profile.setLoyalPartner(approved);
        profile.setReviewedBy(currentReviewer());
        profile.setReviewedAt(LocalDateTime.now());
        adminProfileRepository.save(profile);
        return toResponse(merchant);
    }

    private Merchant findMerchant(UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy merchant"));
    }

    private MerchantAdminProfile getOrCreateProfile(Merchant merchant) {
        return adminProfileRepository.findByMerchantId(merchant.getId())
                .orElseGet(() -> {
                    MerchantAdminProfile profile = new MerchantAdminProfile();
                    profile.setMerchant(merchant);
                    return profile;
                });
    }

    private AdminMerchantResponse toResponse(Merchant merchant) {
        MerchantAdminProfile profile = adminProfileRepository.findByMerchantId(merchant.getId()).orElse(null);
        User user = merchant.getUser();
        return new AdminMerchantResponse(
                merchant.getId(),
                user != null ? user.getDisplayName() : null,
                merchant.getRestaurantName(),
                merchant.getEmail(),
                merchant.getPhone(),
                merchant.getAddress(),
                merchant.getOpenTime(),
                merchant.getCloseTime(),
                merchant.getMerchantStatus(),
                user != null && Boolean.TRUE.equals(user.getIsActive()),
                profile != null && profile.isLoyalPartner(),
                profile != null ? profile.getReviewNote() : null,
                profile != null ? profile.getReviewedBy() : null,
                profile != null ? profile.getReviewedAt() : null
        );
    }

    private void saveMerchantAndUser(Merchant merchant) {
        if (merchant.getUser() != null) {
            userRepository.save(merchant.getUser());
        }
        merchantRepository.save(merchant);
    }

    private boolean matchesKeyword(Merchant merchant, String keyword) {
        if (keyword == null) {
            return true;
        }
        return contains(merchant.getRestaurantName(), keyword)
                || contains(merchant.getEmail(), keyword)
                || contains(merchant.getPhone(), keyword)
                || (merchant.getUser() != null && contains(merchant.getUser().getDisplayName(), keyword));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toUpperCase(Locale.ROOT).contains(keyword);
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String defaultStatus(String status) {
        String normalized = normalize(status);
        return normalized == null || BLOCKED.equals(normalized) ? PENDING : normalized;
    }

    private String currentReviewer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "SYSTEM" : authentication.getName();
    }
}
