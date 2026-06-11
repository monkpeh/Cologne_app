package com.example.colognerecommendation.config;

import com.example.colognerecommendation.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Spring Security configuration for the Cologne Advisor application.
 *
 * <p>Defines three beans that together wire up authentication and authorisation:
 * <ol>
 *   <li>{@link #passwordEncoder()} – selects BCrypt as the hashing algorithm.</li>
 *   <li>{@link #userDetailsService(UserRepository)} – bridges Spring Security's
 *       authentication mechanism to the application's own {@code app_user} database table.</li>
 *   <li>{@link #filterChain(HttpSecurity)} – declares which URLs are public, which require
 *       login, and which are restricted to admins only.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Declares BCrypt as the password hashing algorithm used throughout the application.
     *
     * <p>BCrypt is slow by design — it applies a configurable cost factor (default 10 rounds)
     * that makes brute-force and rainbow-table attacks computationally expensive.
     * Spring Security automatically uses this bean when verifying login attempts and when
     * {@link com.example.colognerecommendation.controller.AuthController} encodes new passwords.
     *
     * @return a {@link BCryptPasswordEncoder} with the default cost factor
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Tells Spring Security how to load an account by username during login.
     *
     * <p>When a user submits the login form, Spring Security calls this service with the
     * entered username. The service looks up the {@link com.example.colognerecommendation.model.AppUser}
     * from the database and wraps it in a Spring Security {@link User} object containing
     * the username, hashed password, and granted roles.
     *
     * <p>Spring Security then compares the entered password against the stored hash using the
     * configured {@link PasswordEncoder}. If they match, the user is authenticated.
     *
     * @param repo the JPA repository used to look up user accounts
     * @return a {@link UserDetailsService} backed by the application's database
     * @throws UsernameNotFoundException if no account exists for the supplied username
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Configures the HTTP security filter chain — the rules that govern which requests
     * are allowed, redirected to login, or blocked with 403 Forbidden.
     *
     * <p><b>Access rules (evaluated top to bottom, first match wins):</b>
     * <ul>
     *   <li>{@code /login} and {@code /register} are publicly accessible — no login required.</li>
     *   <li>{@code /admin/**} requires the {@code ADMIN} role; regular users receive 403.</li>
     *   <li>All other requests require any authenticated user.</li>
     * </ul>
     *
     * <p><b>Form login:</b> Spring Security serves and processes the login form at {@code /login}.
     * On success the user is redirected to {@code /collection}. On failure Spring Security
     * appends {@code ?error} to the login URL so the template can display an error message.
     *
     * <p><b>Logout:</b> A POST to {@code /logout} (handled automatically by Spring Security)
     * invalidates the session and redirects to {@code /login?logout}.
     *
     * <p><b>CSRF protection:</b> Enabled by default. Every non-GET form must use
     * {@code th:action} (not plain {@code action}) so Thymeleaf injects the hidden CSRF token.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link org.springframework.security.web.SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/collection", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
