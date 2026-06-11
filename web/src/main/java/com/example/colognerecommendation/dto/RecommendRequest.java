package com.example.colognerecommendation.dto;

/** Request body for the recommendation engine: weather band + occasion. */
public record RecommendRequest(String weather, String occasion) {}
