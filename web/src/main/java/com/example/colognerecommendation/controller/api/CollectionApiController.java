package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.service.FragranceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for managing the authenticated user's personal fragrance collection.
 *
 * <p>All routes require a valid JWT. The username is extracted from the
 * {@link org.springframework.security.core.context.SecurityContext} via
 * {@link AuthenticationPrincipal} so no user ID is needed in the URL.
 */
@RestController
@RequestMapping("/api/collection")
public class CollectionApiController {

    private final FragranceService fragranceService;

    public CollectionApiController(FragranceService fragranceService) {
        this.fragranceService = fragranceService;
    }

    /**
     * Returns the authenticated user's collection with optional sort and filter.
     *
     * @param sort   one of: name (default), brand, projection, longevity, scentFamily
     * @param filter one of: all (default), office, casual
     */
    @GetMapping
    public ResponseEntity<?> getCollection(
            @RequestParam(defaultValue = "name")  String sort,
            @RequestParam(defaultValue = "all")   String filter,
            @AuthenticationPrincipal UserDetails  userDetails) {

        String username = userDetails.getUsername();
        return ResponseEntity.ok(Map.of(
                "collection",  fragranceService.getSortedFilteredCollection(sort, filter, username),
                "ratings",     fragranceService.getUserRatings(username),
                "suggestions", fragranceService.getSuggestions(username)
        ));
    }

    /** Adds a fragrance to the authenticated user's collection. */
    @PostMapping("/{id}")
    public ResponseEntity<?> addToCollection(@PathVariable int id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        fragranceService.addToCollection(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Added to collection."));
    }

    /** Removes a fragrance from the authenticated user's collection. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCollection(@PathVariable int id,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        fragranceService.removeFromCollection(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Removed from collection."));
    }

    /**
     * Saves or clears a star rating for a fragrance in the user's collection.
     *
     * @param rating star value 1–5; send 0 to clear the rating
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateFragrance(@PathVariable int id,
                                           @RequestParam int rating,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        fragranceService.rateFragrance(id, rating, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Rating saved."));
    }
}
