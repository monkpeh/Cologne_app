package com.example.colognerecommendation.repository;

import com.example.colognerecommendation.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AppUser} entities.
 *
 * <p>Spring generates a concrete implementation at startup. Method names follow
 * Spring Data's query derivation convention so no SQL needs to be written for
 * standard lookups. The bulk-delete method uses a native SQL query because
 * JPQL cannot target {@code @ElementCollection} join tables directly.
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Looks up a user by their unique login name.
     * Used by Spring Security during authentication and by the service layer
     * to retrieve the authenticated user's collection.
     *
     * @param username the login name to search for
     * @return an {@link Optional} containing the user if found, or empty if not
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Checks whether a username is already registered without loading the full entity.
     * Used during registration to detect duplicate usernames before creating the account.
     *
     * @param username the login name to check
     * @return {@code true} if at least one account with this username exists
     */
    boolean existsByUsername(String username);

    /**
     * Removes a fragrance ID from every user's collection in a single SQL statement.
     *
     * <p>Called automatically when an admin deletes a fragrance from the catalogue,
     * ensuring no user's collection holds a dead reference to a non-existent fragrance.
     * A native query is required because JPQL cannot directly target the
     * {@code user_collection} element-collection table.
     *
     * @param id the fragrance ID to remove from all collections
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_collection WHERE fragrance_id = :id", nativeQuery = true)
    void removeFragranceFromAllCollections(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_ratings WHERE fragrance_id = :id", nativeQuery = true)
    void removeFragranceFromAllRatings(@Param("id") int id);
}
