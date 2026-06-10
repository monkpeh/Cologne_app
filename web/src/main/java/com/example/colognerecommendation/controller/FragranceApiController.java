package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.service.FragranceService;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;
import java.util.*;

@RestController
@RequestMapping("/api/fragrances")
public class FragranceApiController {
    /**
     * The service layer for fragrance operations.
     */
    private final FragranceService service;

    /**
     * Constructs a new FragranceApiController with the provided service.
     *
     * @param service the service layer for fragrance operations
     */
    public FragranceApiController(FragranceService service) {
        this.service = service;
    }

    /**
     * Retrieves all fragrances from the database.
     *
     * @return a list of all fragrances
     */
    @GetMapping
    public List<Fragrance> getAllFragrances() {
        return service.getAllFragrances();
    }

    /**
        @return a fragrance by its ID or 404 if the fragrance is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Fragrance> getById(@PathVariable int id) {
         return service.findFragranceById(id)
                 .map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }

    /**
     *
     * @param q the search query
     * @return a list of fragrances that match the search query
     */
    @GetMapping("/search")
    public List<Fragrance> search(@RequestParam String q) {
        return service.searchFragrances(q);
    }

    /**
     * Deletes a fragrance by its ID.
     *
     * @param id the ID of the fragrance to delete
     * @return a 200 response on success or 404 if the fragrance is not found
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFragrance(@PathVariable int id) {
        service.deleteFragrance(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Adds a new fragrance to the database.
     *
     * @param fragrance the fragrance to add
     * @return the added fragrance or 404 if the fragrance is not found
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Fragrance> addFragrance(@Valid @RequestBody Fragrance fragrance) {
        service.saveFragrance(fragrance);
        service.syncToJson();
        return ResponseEntity.ok(fragrance);
    }

    /**
     *  Update an existing fragrance by its ID.
     * @param id
     * @param fragrance
     * @return the updated fragrance or 404 if the fragrance is not found
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Fragrance> updateFragrance(@PathVariable int id, @Valid @RequestBody Fragrance fragrance) {
        return service.findFragranceById(id)
                .map(existing -> {
                    fragrance.id = id;
                    service.saveFragrance(fragrance);
                    service.syncToJson();
                    return ResponseEntity.ok(fragrance);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a list of fragrances to compare.
     * @param ids a list of fragrance IDs to compare
     * @return a list of fragrances to compare or 400 if the list is empty or contains less than 2 IDs
     */
    @GetMapping("/compare")
    public ResponseEntity<List<Fragrance>> compare(@RequestParam List<Integer> ids) {
        if (ids == null || ids.size() < 2) {
            return ResponseEntity.badRequest().build();
        }
        List<Fragrance> fragrances = ids.stream()
                .limit(3)
                .map(service::findFragranceById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fragrances);
    }

}
