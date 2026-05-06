package com.example.colognerecommendation.model;

/**
 * Represents the current outdoor temperature range used as input to the recommendation engine.
 *
 * <p>Each constant maps to a human-readable label shown in the recommendation form dropdown,
 * and drives the weather-score calculation inside {@link com.example.colognerecommendation.engine.RecommendationEngine}.
 * Intermediate values (WARM, MILD, COOL) blend the fragrance's hot and cold season scores
 * using weighted averages so the transition between extremes feels gradual.
 */
public enum Weather {

    /** Above 30 °C – full weight given to a fragrance's hot-season score. */
    HOT("Hot (30°C+)"),

    /** 20–30 °C – 70 % hot-season score, 30 % cold-season score. */
    WARM("Warm (20–30°C)"),

    /** 10–20 °C – equal blend of hot and cold season scores. */
    MILD("Mild (10–20°C)"),

    /** 5–10 °C – 30 % hot-season score, 70 % cold-season score. */
    COOL("Cool (5–10°C)"),

    /** Below 5 °C – full weight given to a fragrance's cold-season score. */
    COLD("Cold (Below 5°C)");

    /** The user-facing string displayed in the recommendation form dropdown. */
    private final String label;

    Weather(String label) { this.label = label; }

    /**
     * Returns the human-readable label for this weather condition.
     *
     * @return a descriptive string such as {@code "Hot (30°C+)"}
     */
    public String getLabel() { return label; }
}
