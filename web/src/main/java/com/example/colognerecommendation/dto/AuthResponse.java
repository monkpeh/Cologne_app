package com.example.colognerecommendation.dto;

/** Successful auth response: JWT token + caller's identity. */
public record AuthResponse(String token, String username, String role) {}
