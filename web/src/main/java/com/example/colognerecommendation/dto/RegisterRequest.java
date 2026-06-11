package com.example.colognerecommendation.dto;

/** Registration request body: username + password + confirmation. */
public record RegisterRequest(String username, String password, String confirmPassword) {}
