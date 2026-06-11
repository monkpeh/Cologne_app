package com.example.colognerecommendation.dto;

/** Request body for creating or updating a fragrance (admin and user submission). */
public record FragranceRequest(
        String  brand,
        String  name,
        String  scentFamily,
        int     projection,
        int     longevity,
        int     seasonHot,
        int     seasonCold,
        boolean officeSafe,
        String  description,
        String  imageUrl
) {}
