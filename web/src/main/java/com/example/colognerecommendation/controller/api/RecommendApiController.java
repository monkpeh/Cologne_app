package com.example.colognerecommendation.controller.api;

import com.example.colognerecommendation.dto.RecommendRequest;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoint for the recommendation engine.
 *
 * <p>{@code POST /api/recommend} accepts a weather band and an occasion, runs the
 * scoring algorithm against the caller's collection, and returns up to 3 ranked results
 * with explanations. The React page calls {@code GET /api/fragrances/meta} first to
 * populate the dropdown options.
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendApiController {

    private final FragranceService fragranceService;

    public RecommendApiController(FragranceService fragranceService) {
        this.fragranceService = fragranceService;
    }

    /**
     * Returns ranked recommendations for the given weather + occasion combination.
     *
     * @param req {@code { "weather": "HOT", "occasion": "DATE" }}
     * @return up to 3 {@link com.example.colognerecommendation.engine.RecommendationResult}
     *         objects, or an empty list when the user's collection is empty
     */
    @PostMapping
    public ResponseEntity<?> recommend(@RequestBody RecommendRequest req,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Weather  weather  = Weather.valueOf(req.weather().toUpperCase());
            Occasion occasion = Occasion.valueOf(req.occasion().toUpperCase());

            return ResponseEntity.ok(Map.of(
                    "results", fragranceService.getRecommendations(weather, occasion, userDetails.getUsername())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid weather or occasion value."));
        }
    }
}
