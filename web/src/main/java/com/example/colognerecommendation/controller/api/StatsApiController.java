package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.service.FragranceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoint for the authenticated user's collection statistics dashboard.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsApiController {

    private final FragranceService fragranceService;

    public StatsApiController(FragranceService fragranceService) {
        this.fragranceService = fragranceService;
    }

    /**
     * Returns all computed statistics for the caller's collection:
     * totals, average rating, scent-family breakdown, standouts, and top-rated list.
     */
    @GetMapping
    public ResponseEntity<?> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(Map.of(
                "stats",   fragranceService.getStats(username),
                "ratings", fragranceService.getUserRatings(username)
        ));
    }
}
