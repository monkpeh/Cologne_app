package com.example.colognerecommendation.model;

/**
 * Represents the social context in which the fragrance will be worn.
 *
 * <p>Each constant applies different scoring rules inside
 * {@link com.example.colognerecommendation.engine.RecommendationEngine}:
 * <ul>
 *   <li>{@code OFFICE} heavily penalises non-office-safe fragrances and high projection.</li>
 *   <li>{@code DATE} rewards high projection and longevity for a memorable impression.</li>
 *   <li>{@code SOCIAL} rewards projection-heavy fragrances suited to crowded environments.</li>
 *   <li>{@code CASUAL} prefers moderate projection (rating of 3) for everyday comfort.</li>
 *   <li>{@code FORMAL} balances office-appropriateness with moderate-to-good projection.</li>
 * </ul>
 */
public enum Occasion {

    /** Everyday relaxed wear – moderate projection preferred. */
    CASUAL("Casual / Everyday"),

    /** Professional environment – office-safe fragrances with low-to-moderate projection. */
    OFFICE("Office / Work"),

    /** Romantic setting – high projection and longevity rewarded. */
    DATE("Date Night"),

    /** Black-tie or ceremony – sophisticated, office-appropriate scents preferred. */
    FORMAL("Formal Event"),

    /** Party or gathering – strong projection suited to busy, loud environments. */
    SOCIAL("Social / Party");

    /** The user-facing string displayed in the recommendation form dropdown. */
    private final String label;

    Occasion(String label) { this.label = label; }

    /**
     * Returns the human-readable label for this occasion.
     *
     * @return a descriptive string such as {@code "Office / Work"}
     */
    public String getLabel() { return label; }
}
