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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

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
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Public (Xem trang chủ, tìm kiếm, nhà hàng, món ăn, danh mục, auth, static files)
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
                                "/api/auth/**",
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
                                "/api/merchants/register"
                        ).hasRole("USER")

                        // Admin
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // Merchant
                        .requestMatchers(
                                "/merchant/**",
                                "/api/merchant/**"
                        ).hasAnyRole("MERCHANT", "ADMIN")

                        // API lấy thông tin Merchant
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/merchants/**"
                        ).permitAll()

                        // User
                        .requestMatchers(
                                "/user/**",
                                "/api/user/**"
                        ).authenticated()

                        // Các request còn lại
                        .anyRequest()
                        .authenticated()
                )

                // Nếu chưa đăng nhập mà truy cập trang cần đăng nhập
                // thì chuyển về /login
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )

                // JWT -> không sử dụng session
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // JWT Filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}