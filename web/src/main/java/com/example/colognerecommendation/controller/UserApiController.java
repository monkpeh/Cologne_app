package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.dto.RecommendationRequest;
import com.example.colognerecommendation.engine.RecommendationResult;
import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.model.UserStats;
import com.example.colognerecommendation.model.Weather;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.repository.UserRepository;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.colognerecommendation.dto.RatingRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import com.example.colognerecommendation.dto.RecommendationResponse;
import com.example.colognerecommendation.dto.ScoredFragranceDto;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    /**
     * The service layer for user operations.
     */
    private final UserRepository userRepository;
    private final FragranceService service;

    /**
     * Constructs a new UserApiController with the provided service.
     * @param userRepository
     * @param service
     */
    public UserApiController(UserRepository userRepository, FragranceService service) {
        this.userRepository = userRepository;
        this.service = service;
    }

    /**
     * Retrieves the current user based on the principal(username).
     * @param principal
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<AppUser> getMe(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a fragrance to the logged-in user's collection.
     * @param fragranceId the ID of the fragrance to add
     * @param principal the principal of the logged-in user
     * @return a 200 response on success
     */
    @PostMapping("/me/collection/{fragranceId}")
    public ResponseEntity<Void> addToCollection(@PathVariable int fragranceId, Principal principal) {
        service.addToCollection(fragranceId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Removes a fragrance from the logged-in user's collection
     * @param fragranceId 
     * @param principal
     * @return a 200 response on success
     */
    @DeleteMapping("/me/collection/{fragranceId}")
    public ResponseEntity<Void> removeFromCollection(@PathVariable int fragranceId, Principal principal){
        service.removeFromCollection(fragranceId, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Rate a fragrance in the logged-in user's collection
     * @param fragranceId
     * @param req
     * @param principal
     * @return
     */
    @PostMapping("/me/collection/{fragranceId}/rate")
    public ResponseEntity<Void> rateFragrance(@PathVariable int fragranceId, @RequestBody RatingRequest req, Principal principal) {
        service.rateFragrance(fragranceId, req.rating, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the logged-in user's collection of fragrances.
     * @param principal the principal of the logged-in user
     * @return a list of fragrances in the user's collection
     */
    @GetMapping("/me/collection")
    public ResponseEntity<List<Fragrance>> getCollection(Principal principal) {
        return ResponseEntity.ok(service.getUserCollection(principal.getName()));
    }

    /**
     * Retrieves the logged-in user's statistics.
     * @param principal the principal of the logged-in user
     * @return a list of fragrances in the user's collection
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStats> getStats(Principal principal) {
        return ResponseEntity.ok(service.getStats(principal.getName()));
    }

    /**
     * Uses the logged-in user's collection and preferences to generate fragrance recommendations.
     * @param weather the
     * @param occasion
     * @param principal
     * @return a list of fragrance recommendations
     */
    @GetMapping("/me/recommendations")
    public ResponseEntity<List<ScoredFragranceDto>> getRecommendations(
            @RequestParam String weather,
            @RequestParam String occasion,
            Principal principal) {

        String username = principal.getName();
        RecommendationResponse response = service.getRecommendationsViaRabbit(weather, occasion, username);

        List<ScoredFragranceDto> results = response != null ? response.getResults() : Collections.emptyList();
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieves fragrance suggestions for the logged-in user.
     * @param principal the principal of the logged-in user
     * @return a list of fragrance suggestions
     */
    @GetMapping("/me/suggestions")
    public ResponseEntity<List<Fragrance>> getFragranceSuggestions(Principal principal) {
        return ResponseEntity.ok(service.getSuggestions(principal.getName()));
    }
}
