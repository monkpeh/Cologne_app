package com.example.colognerecommendation.engine;

import com.example.colognerecommendation.model.Fragrance;

import java.util.List;

/**
 * Immutable container for a single fragrance recommendation produced by {@link RecommendationEngine}.
 *
 * <p>Holds the fragrance itself, its composite score in the range [0.0, 1.0], and a list
 * of plain-English reasons explaining why it ranked where it did. These reasons are rendered
 * in the "Why this fragrance?" section on the recommendation page.
 */
public class RecommendationResult {

    private final Fragrance    fragrance;
    private final double       score;
    private final List<String> reasons;

    /**
     * Constructs a fully populated result. Called exclusively by {@link RecommendationEngine}.
     *
     * @param fragrance the fragrance that was scored
     * @param score     composite score in [0.0, 1.0] (weather 40% + occasion 40% + longevity 20%)
     * @param reasons   human-readable explanations for the score; never empty
     */
    public RecommendationResult(Fragrance fragrance, double score, List<String> reasons) {
        this.fragrance = fragrance;
        this.score     = score;
        this.reasons   = reasons;
    }

    /**
     * Returns the fragrance this result describes.
     *
     * @return the scored {@link Fragrance}
     */
    public Fragrance getFragrance() { return fragrance; }

    /**
     * Returns the raw composite score used for ranking.
     *
     * @return a value between 0.0 (worst match) and 1.0 (perfect match)
     */
    public double getScore() { return score; }

    /**
     * Returns the list of plain-English reasons explaining this result's ranking.
     * Examples: "Excellent choice for hot conditions", "Long-lasting – won't need a reapplication".
     *
     * @return an unmodifiable list of reason strings; at least one entry is always present
     */
    public List<String> getReasons() { return reasons; }

    /**
     * Converts the raw score to a whole-number percentage string for display in templates.
     * A score of 0.876 becomes {@code "88"}, for example.
     *
     * @return the score as a rounded percentage string (e.g. {@code "75"})
     */
    public String getScorePercent() {
        return String.format("%.0f", score * 100);
    }
}
