package vn.codegyme.meal_choice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMerchantModelAdvice {

    private static final String CACHED_MERCHANT_KEY = "CACHED_GLOBAL_MERCHANT";
    private final MerchantRepository merchantRepository;

    @ModelAttribute("currentMerchantBlocked")
    public boolean currentMerchantBlocked(Authentication authentication, HttpServletRequest request) {
        Merchant merchant = getCachedMerchant(authentication, request);
        return merchant != null && merchant.getMerchantStatus() == MerchantStatus.BLOCKED;
    }

    @ModelAttribute("merchantLockReason")
    public String merchantLockReason(Authentication authentication, HttpServletRequest request) {
        Merchant merchant = getCachedMerchant(authentication, request);
        if (merchant == null || merchant.getMerchantStatus() != MerchantStatus.BLOCKED) {
            return null;
        }
        return merchant.getLockReason();
    }

    private Merchant getCachedMerchant(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        if (request != null && request.getAttribute(CACHED_MERCHANT_KEY) != null) {
            Object cached = request.getAttribute(CACHED_MERCHANT_KEY);
            return cached instanceof Merchant m ? m : null;
        }

        Merchant merchant = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            merchant = merchantRepository.findByUser_Id(userDetails.getId()).orElse(null);
        }

        if (request != null) {
            request.setAttribute(CACHED_MERCHANT_KEY, merchant != null ? merchant : new Object());
        }

        return merchant;
    }
}
