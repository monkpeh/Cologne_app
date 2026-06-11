package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.dto.AuthRequest;
import com.example.colognerecommendation.dto.AuthResponse;
import com.example.colognerecommendation.dto.RegisterRequest;
import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.repository.UserRepository;
import com.example.colognerecommendation.security.AppUserDetailsService;
import com.example.colognerecommendation.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for login and registration.
 *
 * <p>Both endpoints are publicly accessible (no JWT required) and return a signed
 * JWT token on success. The React frontend stores this token and sends it as
 * {@code Authorization: Bearer <token>} on every subsequent request.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager   authManager;
    private final AppUserDetailsService   userDetailsService;
    private final JwtUtil                 jwtUtil;
    private final UserRepository          userRepository;
    private final PasswordEncoder         passwordEncoder;

    public AuthApiController(AuthenticationManager   authManager,
                             AppUserDetailsService   userDetailsService,
                             JwtUtil                 jwtUtil,
                             UserRepository          userRepository,
                             PasswordEncoder         passwordEncoder) {
        this.authManager        = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil            = jwtUtil;
        this.userRepository     = userRepository;
        this.passwordEncoder    = passwordEncoder;
    }

    /**
     * Authenticates the user and returns a JWT token.
     *
     * @param req username + password
     * @return 200 with {@link AuthResponse}, or 401 on bad credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username());
        String token = jwtUtil.generateToken(userDetails);
        AppUser user = userRepository.findByUsername(req.username()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    /**
     * Creates a new user account and returns a JWT token so the caller is immediately logged in.
     *
     * <p>All new accounts receive the {@code USER} role. The admin account is always
     * seeded by {@link com.example.colognerecommendation.config.DataInitializer} at startup.
     *
     * @param req username + password + confirmPassword
     * @return 201 with {@link AuthResponse}, or 400/409 on validation failure
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username required; password must be at least 6 characters."));
        }
        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Passwords do not match."));
        }
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken."));
        }

        AppUser user = new AppUser();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole("USER");
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}
