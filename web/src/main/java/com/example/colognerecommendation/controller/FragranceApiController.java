package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.service.FragranceService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
}
