package com.example.colognerecommendation.dto;

/** Login request body: username + password. */
public record AuthRequest(String username, String password) {}
