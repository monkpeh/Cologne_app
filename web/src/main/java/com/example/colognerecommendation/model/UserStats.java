package com.example.colognerecommendation.model;

import java.util.List;
import java.util.Map;

/**
 * View model (DTO) carrying all computed statistics for the /stats dashboard.
 *
 * <p>This is a pure POJO — no JPA annotations. It is constructed once per request
 * inside {@link com.example.colognerecommendation.service.FragranceService#getStats}
 * and placed in the Thymeleaf model under the key {@code "stats"}.
 *
 * <p><b>Why getters?</b> Thymeleaf's SpEL evaluates {@code ${stats.totalOwned}} by
 * looking for a method named {@code getTotalOwned()} via Java reflection (JavaBean convention).
 * Without a getter, the template throws a {@code SpelEvaluationException} at runtime.
 */
public class UserStats {

    private final int totalOwned;
    private final int totalRated;
    private final double averageRating;
    private final Map<String, Long> familyCounts;
    private final int officeSafeCount;
    private final int casualCount;
    private final List<Fragrance> topRated;
    private final Fragrance mostProjecting;
    private final Fragrance longestLasting;

    public UserStats(int totalOwned,
                     int totalRated,
                     double averageRating,
                     Map<String, Long> familyCounts,
                     int officeSafeCount,
                     int casualCount,
                     List<Fragrance> topRated,
                     Fragrance mostProjecting,
                     Fragrance longestLasting) {
        this.totalOwned      = totalOwned;
        this.totalRated      = totalRated;
        this.averageRating   = averageRating;
        this.familyCounts    = familyCounts;
        this.officeSafeCount = officeSafeCount;
        this.casualCount     = casualCount;
        this.topRated        = topRated;
        this.mostProjecting  = mostProjecting;
        this.longestLasting  = longestLasting;
    }

    public int getTotalOwned()                 { return totalOwned; }
    public int getTotalRated()                 { return totalRated; }
    public double getAverageRating()           { return averageRating; }
    public Map<String, Long> getFamilyCounts() { return familyCounts; }
    public int getOfficeSafeCount()            { return officeSafeCount; }
    public int getCasualCount()                { return casualCount; }
    public List<Fragrance> getTopRated()       { return topRated; }
    public Fragrance getMostProjecting()       { return mostProjecting; }
    public Fragrance getLongestLasting()       { return longestLasting; }
}