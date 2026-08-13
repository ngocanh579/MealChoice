package vn.codegyme.meal_choice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantStatus;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MerchantBlockedFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;


    /**
     * Chỉ kiểm tra các request đi vào kênh Merchant.
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        /*
         * User bình thường vẫn phải được vào
         * trang đăng ký Merchant.
         */
        if (uri.equals("/merchant/register")) {
            return true;
        }

        /*
         * Chỉ chạy Filter với:
         *
         * /merchant/**
         * /api/merchant/**
         */
        return !uri.startsWith("/merchant/")
                && !uri.startsWith("/api/merchant/");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // =========================================
        // 1. CHƯA ĐĂNG NHẬP
        // =========================================

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =========================================
        // 2. LẤY EMAIL USER ĐANG ĐĂNG NHẬP
        // =========================================

        String email =
                authentication.getName();


        // =========================================
        // 3. TÌM USER
        // =========================================

        User user = userRepository
                .findByEmail(email)
                .orElse(null);


        if (user == null) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =========================================
        // 4. TÌM MERCHANT CỦA USER
        // =========================================
        /*
         * Không sửa MerchantRepository.
         *
         * Dùng findAll() có sẵn từ JpaRepository
         * rồi tìm Merchant thuộc User hiện tại.
         */

        Merchant merchant = merchantRepository
                .findAll()
                .stream()
                .filter(m ->
                        m.getUser() != null
                                && m.getUser()
                                .getId()
                                .equals(user.getId())
                )
                .findFirst()
                .orElse(null);


        // =========================================
        // 5. USER KHÔNG CÓ MERCHANT
        // =========================================

        if (merchant == null) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =========================================
        // 6. MERCHANT BỊ KHÓA
        // =========================================

        if (merchant.getMerchantStatus()
                == MerchantStatus.BLOCKED) {

            /*
             * API thì không nên redirect HTML.
             */
            if (request.getRequestURI()
                    .startsWith("/api/merchant/")) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                );

                response.setContentType(
                        "application/json;charset=UTF-8"
                );

                response.getWriter().write(
                        """
                        {
                          "error": "MERCHANT_BLOCKED",
                          "message": "Merchant đã bị khóa."
                        }
                        """
                );

                return;
            }


            /*
             * Trang Thymeleaf:
             * chuyển sang trang thông báo bị khóa.
             */
            response.sendRedirect(
                    "/merchant-blocked"
            );

            return;
        }


        // =========================================
        // 7. MERCHANT KHÔNG BỊ KHÓA
        // =========================================

        filterChain.doFilter(
                request,
                response
        );
    }
}
