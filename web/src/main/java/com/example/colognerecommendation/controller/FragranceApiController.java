package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.service.FragranceService;
import com.example.colognerecommendation.repository.*;
import com.example.colognerecommendation.engine.*;

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
}
