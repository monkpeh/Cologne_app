package com.example.colognerecommendation.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * JPA entity representing a registered user account.
 *
 * <p>Persisted to the {@code app_user} table (the name "user" is reserved in H2 SQL,
 * so the table is explicitly renamed via {@code @Table}).
 *
 * <p>Each user owns a personal fragrance collection stored as an {@code @ElementCollection}
 * in a separate {@code user_collection} join table. The collection holds only fragrance IDs
 * (integers), keeping the data model simple — the full {@link Fragrance} objects are resolved
 * at query time by {@link com.example.colognerecommendation.service.FragranceService}.
 *
 * <p>Passwords are never stored in plain text. The
 * {@link com.example.colognerecommendation.config.SecurityConfig} configures BCrypt hashing,
 * and {@link com.example.colognerecommendation.controller.AuthController} encodes passwords
 * before calling {@code setPassword}.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login name chosen at registration. */
    @Column(unique = true, nullable = false)
    private String username;

    /** BCrypt-hashed password. Never the plain-text value entered by the user. */
    @Column(nullable = false)
    private String password;

    /**
     * Security role controlling access to protected routes.
     * Valid values are {@code "USER"} (default) and {@code "ADMIN"}.
     * Spring Security prefixes this with {@code "ROLE_"} internally.
     * The first account ever registered is automatically assigned {@code "ADMIN"}.
     */
    @Column(nullable = false)
    private String role = "USER";

    /**
     * The set of fragrance IDs this user has added to their collection.
     * Stored in the {@code user_collection} table with a foreign key back to {@code app_user}.
     * {@code FetchType.EAGER} ensures the IDs are loaded alongside the user in a single query.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_collection", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "fragrance_id")
    private Set<Integer> collectionIds = new LinkedHashSet<>();

    /**
     * Per-fragrance star ratings given by this user, keyed by fragrance ID.
     * Values are in [1, 5]. Stored in the {@code user_ratings} join table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_ratings", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "fragrance_id")
    @Column(name = "rating")
    private Map<Integer, Integer> ratings = new HashMap<>();

    // ── Getters & setters ─────────────────────────────────────────────────────

    /** @return the database primary key */
    public Long getId() { return id; }

    /** @return the user's login username */
    public String getUsername() { return username; }

    /** @param username the login name to assign */
    public void setUsername(String username) { this.username = username; }

    /** @return the BCrypt-hashed password */
    public String getPassword() { return password; }

    /** @param password a BCrypt-encoded password hash (never plain text) */
    public void setPassword(String password) { this.password = password; }

    /**
     * Returns the live, mutable set of fragrance IDs in this user's collection.
     * Callers may add or remove IDs directly; changes are persisted when the entity is saved.
     *
     * @return the mutable collection ID set
     */
    public Set<Integer> getCollectionIds() { return collectionIds; }

    /** @return {@code "USER"} or {@code "ADMIN"} */
    public String getRole() { return role; }

    /** @param role the role to assign — must be {@code "USER"} or {@code "ADMIN"} */
    public void setRole(String role) { this.role = role; }

    /** @return the mutable map of fragrance ID → star rating (1–5) */
    public Map<Integer, Integer> getRatings() { return ratings; }
}