package vn.codegyme.meal_choice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMerchantModelAdvice {

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


        /*
         * Không cần sửa MerchantRepository hiện tại.
         */
        return merchantRepository
                .findAll()
                .stream()
                .filter(merchant ->

                        merchant.getUser() != null

                                && merchant.getUser()
                                .getId()
                                .equals(user.getId())
                )
                .findFirst()
                .orElse(null);
    }
}
