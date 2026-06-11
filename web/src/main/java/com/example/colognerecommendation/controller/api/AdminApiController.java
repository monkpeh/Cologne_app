package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.dto.FragranceRequest;
import com.example.colognerecommendation.dto.PasswordResetRequest;
import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.repository.UserRepository;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Admin-only REST endpoints for user management and fragrance catalogue CRUD.
 *
 * <p>All routes under {@code /api/admin/**} require the {@code ADMIN} role
 * (enforced by {@link com.example.colognerecommendation.config.SecurityConfig}).
 *
 * <p>Safety guards mirror the old Thymeleaf admin panel:
 * <ul>
 *   <li>Admin cannot delete their own account.</li>
 *   <li>Admin cannot demote their own role.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final FragranceService fragranceService;

    public AdminApiController(UserRepository   userRepository,
                              PasswordEncoder  passwordEncoder,
                              FragranceService fragranceService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fragranceService = fragranceService;
    }

    // ── User management ───────────────────────────────────────────────────────

    /** Returns all registered accounts (passwords are excluded by {@code @JsonIgnore}). */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /** Deletes a user account. Admins cannot delete their own account. */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails caller) {
        Optional<AppUser> target = userRepository.findById(id);
        if (target.isEmpty()) return ResponseEntity.notFound().build();
        if (target.get().getUsername().equals(caller.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete your own account."));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted."));
    }

    /** Resets another user's password. Minimum 6 characters enforced. */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestBody PasswordResetRequest req) {
        if (req.newPassword() == null || req.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }
        Optional<AppUser> target = userRepository.findById(id);
        if (target.isEmpty()) return ResponseEntity.notFound().build();

        AppUser user = target.get();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset."));
    }

    /** Toggles a user's role between USER and ADMIN. Admins cannot demote themselves. */
    @PostMapping("/users/{id}/toggle-role")
    public ResponseEntity<?> toggleRole(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails caller) {
        Optional<AppUser> target = userRepository.findById(id);
        if (target.isEmpty()) return ResponseEntity.notFound().build();

        AppUser user = target.get();
        if (user.getUsername().equals(caller.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot change your own role."));
        }
        user.setRole("ADMIN".equals(user.getRole()) ? "USER" : "ADMIN");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Role updated.", "newRole", user.getRole()));
    }

    // ── Fragrance catalogue management ────────────────────────────────────────

    /** Returns the full fragrance catalogue for the admin management table. */
    @GetMapping("/fragrances")
    public ResponseEntity<?> listFragrances() {
        return ResponseEntity.ok(fragranceService.getAllFragrances());
    }

    /** Creates a new fragrance and syncs the JSON seed file. */
    @PostMapping("/fragrances")
    public ResponseEntity<?> createFragrance(@RequestBody FragranceRequest req) {
        Fragrance saved = fragranceService.saveFragrance(CatalogueApiController.mapRequest(req));
        fragranceService.syncToJson();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Updates an existing fragrance by ID and syncs the JSON seed file. */
    @PutMapping("/fragrances/{id}")
    public ResponseEntity<?> updateFragrance(@PathVariable int id,
                                             @RequestBody FragranceRequest req) {
        Optional<Fragrance> existing = fragranceService.findFragranceById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Fragrance f = CatalogueApiController.mapRequest(req);
        f.id = id;
        Fragrance saved = fragranceService.saveFragrance(f);
        fragranceService.syncToJson();
        return ResponseEntity.ok(saved);
    }

    /** Deletes a fragrance and removes it from all user collections. */
    @DeleteMapping("/fragrances/{id}")
    public ResponseEntity<?> deleteFragrance(@PathVariable int id) {
        if (fragranceService.findFragranceById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        fragranceService.deleteFragrance(id);
        fragranceService.syncToJson();
        return ResponseEntity.ok(Map.of("message", "Fragrance deleted."));
    }
}
