package vn.codegyme.meal_choice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
=======
>>>>>>> hung
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
<<<<<<< HEAD
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
=======
>>>>>>> hung
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
    private final MerchantBlockedFilter merchantBlockedFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // OPTIONS
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Public
                        .requestMatchers(
                                "/",
                                "/home",
                                "/search/**",
                                "/restaurants/**",
<<<<<<< HEAD
                                "/stores/**",
=======
>>>>>>> hung
                                "/food/**",
                                "/foods/**",
                                "/categories/**",
                                "/api/foods/**",
                                "/api/categories/**",
<<<<<<< HEAD
=======
                                "/api/merchant/stats/**",
>>>>>>> hung
                                "/login",
                                "/register",
                                "/activate",
                                "/api/auth/**",
                                "/merchant-blocked",
                                "/uploads/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // User đã đăng nhập được mở trang đăng ký Merchant
                        .requestMatchers(
                                HttpMethod.GET,
                                "/merchant/register"
                        ).hasRole("USER")

                        // User đăng ký Merchant
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/merchant/register"
                        ).hasRole("USER")

                        // Admin
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // API kiểm tra trạng thái Merchant của User
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/merchant/my-status"
                        ).authenticated()

                        // API thông tin Merchant public
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/merchant/*",
                                "/api/merchants/**"
                        ).permitAll()

<<<<<<< HEAD
                        // API tương tác Merchant (follow / unfollow)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/merchants/*/follow",
                                "/api/merchants/*/unfollow"
                        ).authenticated()

=======
>>>>>>> hung
                        // Merchant
                        .requestMatchers(
                                "/merchant/**",
                                "/api/merchant/**"
                        ).hasAnyRole(
                                "MERCHANT",
                                "ADMIN"
                        )

<<<<<<< HEAD
                        // User / Checkout / Delivery APIs
                        .requestMatchers(
                                "/user/**",
                                "/api/user/**",
                                "/api/checkout/**",
                                "/api/delivery/**"
=======
                        // User
                        .requestMatchers(
                                "/user/**",
                                "/api/user/**"
>>>>>>> hung
                        ).authenticated()

                        // Các request còn lại
                        .anyRequest()
                        .authenticated()
                )

<<<<<<< HEAD
                // Xử lý khi chưa xác thực
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                        .defaultAuthenticationEntryPointFor(
=======
                // Chưa đăng nhập -> /login
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
>>>>>>> hung
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )

                // JWT -> Stateless
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // JWT Filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Kiểm tra Merchant bị khóa
                .addFilterAfter(
                        merchantBlockedFilter,
                        JwtAuthFilter.class
                );

        return http.build();
    }
}