package vn.codegyme.meal_choice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import vn.codegyme.meal_choice.security.JwtAuthFilter;
import vn.codegyme.meal_choice.security.MerchantBlockedFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // THÊM FILTER NÀY
    private final MerchantBlockedFilter merchantBlockedFilter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // OPTIONS
                        // =====================================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        // Public (Xem trang chủ, tìm kiếm, nhà hàng, món ăn, danh mục, auth, static files)

                        // =====================================
                        // PUBLIC
                        // =====================================
                        .requestMatchers(
                                "/",
                                "/home",
                                "/search/**",
                                "/restaurants/**",
                                "/food/**",
                                "/foods/**",
                                "/categories/**",
                                "/api/foods/**",
                                "/api/categories/**",
                                "/login",
                                "/register",
                                "/activate",
                                "/api/auth/**",

                                // Trang báo Merchant bị khóa
                                "/merchant-blocked",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error",
                                "/favicon.ico"
                        )
                        .permitAll()


                        // =====================================
                        // USER ĐĂNG KÝ MERCHANT
                        // =====================================

                        // Mở trang đăng ký Merchant
                        .requestMatchers(
                                HttpMethod.GET,
                                "/merchant/register"
                        )
                        .hasRole("USER")


                        // Gửi đăng ký Merchant
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/merchants/register"
                        )
                        .hasRole("USER")


                        // =====================================
                        // ADMIN
                        // =====================================
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasRole("ADMIN")


                        // =====================================
                        // MERCHANT
                        // =====================================
                        .requestMatchers(
                                "/merchant/**",
                                "/api/merchant/**"
                        )
                        .hasAnyRole(
                                "MERCHANT",
                                "ADMIN"
                        )


                        // =====================================
                        // API THÔNG TIN MERCHANT PUBLIC
                        // =====================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/merchants/**"
                        )
                        .permitAll()


                        // =====================================
                        // USER
                        // =====================================
                        .requestMatchers(
                                "/user/**",
                                "/api/user/**"
                        )
                        .authenticated()


                        // =====================================
                        // CÁC REQUEST CÒN LẠI
                        // =====================================
                        .anyRequest()
                        .authenticated()
                )


                // =========================================
                // CHƯA ĐĂNG NHẬP -> /login
                // =========================================
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(
                                        "/login"
                                ),
                                new MediaTypeRequestMatcher(
                                        MediaType.TEXT_HTML
                                )
                        )
                )


                // =========================================
                // JWT -> STATELESS
                // =========================================
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =========================================
                // FILTER 1: XÁC THỰC JWT
                // =========================================
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )


                // =========================================
                // FILTER 2: KIỂM TRA MERCHANT BỊ KHÓA
                // =========================================
                .addFilterAfter(
                        merchantBlockedFilter,
                        JwtAuthFilter.class
                );


        return http.build();
    }
}