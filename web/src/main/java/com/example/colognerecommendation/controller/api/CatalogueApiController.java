package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.dto.FragranceRequest;
import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the fragrance catalogue — browse, search, and user submission.
 *
 * <p>All routes require authentication. Admin-only CRUD lives in
 * {@link AdminApiController}.
 */
@RestController
@RequestMapping("/api/fragrances")
public class CatalogueApiController {

    private final FragranceService fragranceService;

    public CatalogueApiController(FragranceService fragranceService) {
        this.fragranceService = fragranceService;
    }

    /**
     * Returns the full catalogue (or search results) with the caller's collection IDs
     * and community average ratings so the React browse page can show "In Collection" state.
     *
     * @param q optional search term matched against brand, name, and scent family
     */
    @GetMapping
    public ResponseEntity<?> browse(@RequestParam(defaultValue = "") String q,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "fragrances",     fragranceService.searchFragrances(q),
                "collectionIds",  fragranceService.getCollectionIds(userDetails.getUsername()),
                "averageRatings", fragranceService.getAverageRatings()
        ));
    }

    /**
     * Returns the enum metadata (value + label) for Weather and Occasion so the
     * React dropdowns can be populated without hardcoding strings in the frontend.
     */
    @GetMapping("/meta")
    public ResponseEntity<?> meta() {
        List<Map<String, String>> weathers = java.util.Arrays.stream(
                com.example.colognerecommendation.model.Weather.values())
                .map(w -> Map.of("value", w.name(), "label", w.getLabel()))
                .toList();

        List<Map<String, String>> occasions = java.util.Arrays.stream(
                com.example.colognerecommendation.model.Occasion.values())
                .map(o -> Map.of("value", o.name(), "label", o.getLabel()))
                .toList();

        return ResponseEntity.ok(Map.of("weathers", weathers, "occasions", occasions));
    }

    /**
     * Returns the specified fragrances for side-by-side comparison.
     * Only returns fragrances that are in the caller's collection (security guard).
     *
     * @param ids 2–3 fragrance IDs to compare
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compare(@RequestParam List<Integer> ids,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        java.util.Set<Integer> owned = fragranceService.getCollectionIds(username);

        List<Integer> allowed = ids.stream()
                .filter(owned::contains)
                .distinct()
                .limit(3)
                .toList();

        return ResponseEntity.ok(Map.of(
                "fragrances", fragranceService.findFragrancesByIds(allowed),
                "ratings",    fragranceService.getUserRatings(username)
        ));
    }

    /**
     * Allows any authenticated user to submit a new fragrance to the catalogue.
     * The fragrance is automatically added to the submitter's collection.
     */
    @PostMapping
    public ResponseEntity<?> submitFragrance(@RequestBody FragranceRequest req,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Fragrance f = mapRequest(req);
        Fragrance saved = fragranceService.saveFragrance(f);
        fragranceService.syncToJson();
        fragranceService.addToCollection(saved.getId(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Maps a {@link FragranceRequest} DTO onto a new {@link Fragrance} entity. */
    static Fragrance mapRequest(FragranceRequest req) {
        Fragrance f = new Fragrance();
        f.brand      = req.brand();
        f.name       = req.name();
        f.scentFamily = req.scentFamily();
        f.projection = req.projection();
        f.longevity  = req.longevity();
        f.seasonHot  = req.seasonHot();
        f.seasonCold = req.seasonCold();
        f.officeSafe = req.officeSafe();
        f.description = req.description();
        f.imageUrl   = req.imageUrl();
        return f;
    }
}
