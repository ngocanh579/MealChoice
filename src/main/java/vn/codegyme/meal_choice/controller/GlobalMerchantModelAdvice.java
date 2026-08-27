package vn.codegyme.meal_choice.controller;

<<<<<<< HEAD
import jakarta.servlet.http.HttpServletRequest;
=======
>>>>>>> hung
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
<<<<<<< HEAD
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
=======

import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;
>>>>>>> hung

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMerchantModelAdvice {

<<<<<<< HEAD
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
=======
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;


    @ModelAttribute("currentMerchantBlocked")
    public boolean currentMerchantBlocked(
            Authentication authentication
    ) {

        Merchant merchant =
                getCurrentMerchant(authentication);

        return merchant != null
                && merchant.getMerchantStatus()
                == MerchantStatus.BLOCKED;
    }


    @ModelAttribute("merchantLockReason")
    public String merchantLockReason(
            Authentication authentication
    ) {

        Merchant merchant =
                getCurrentMerchant(authentication);

        if (merchant == null) {
            return null;
        }

        if (merchant.getMerchantStatus()
                != MerchantStatus.BLOCKED) {

            return null;
        }

        return merchant.getLockReason();
    }


    private Merchant getCurrentMerchant(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {

            return null;
        }


        String email =
                authentication.getName();


        User user = userRepository
                .findByEmail(email)
                .orElse(null);


        if (user == null) {
            return null;
        }


        return merchantRepository
                .findByUser_Id(user.getId())
                .orElse(null);
>>>>>>> hung
    }
}
