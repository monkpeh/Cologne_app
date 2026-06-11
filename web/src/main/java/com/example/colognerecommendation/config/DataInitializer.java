package com.example.colognerecommendation.config;

import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.repository.UserRepository;
import com.example.colognerecommendation.service.FragranceService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin account on first startup so the app is immediately
 * testable without manual registration.
 *
 * <p>{@link FragranceService} is injected as a constructor argument to guarantee
 * that {@link FragranceService#seedDataset()} completes before this bean's
 * {@link #seedAdminUser()} runs — the admin's collection needs the fragrance IDs
 * to already exist in the database.
 *
 * <p>The seeder is idempotent: if any users already exist in the database the method
 * returns immediately, so restarting the server never creates duplicate accounts.
 *
 * <p>Default credentials ({@code admin} / {@code admin123}) can be changed via
 * {@code application.properties}. To pick up new credentials on a running instance,
 * delete {@code web/data/} and restart.
 */
@Component
public class DataInitializer {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final FragranceService fragranceService;

    @Value("${app.admin.username}") private String adminUsername;
    @Value("${app.admin.password}") private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           FragranceService fragranceService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fragranceService = fragranceService;
    }

    /**
     * Creates the default admin user with every fragrance pre-loaded into their
     * collection, but only when the database contains no users yet.
     *
     * <p>Because {@link FragranceService} is a constructor dependency, Spring has
     * already fully initialised it (including its own {@code @PostConstruct} fragrance
     * seeding) before this method is called, so {@code getAllFragrances()} is guaranteed
     * to return the full catalogue.
     */
    @PostConstruct
    public void seedAdminUser() {
        if (userRepository.existsByUsername(adminUsername)) return;
        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");

        fragranceService.getAllFragrances()
                        .forEach(f -> admin.getCollectionIds().add(f.getId()));

        userRepository.save(admin);
    }
}
