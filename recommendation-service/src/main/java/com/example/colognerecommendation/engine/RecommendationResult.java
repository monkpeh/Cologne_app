package com.example.colognerecommendation.engine;

import com.example.colognerecommendation.dto.FragranceDto;

import java.util.List;

public class RecommendationResult {

    private final FragranceDto fragrance;
    private final double       score;
    private final List<String> reasons;

    public RecommendationResult(FragranceDto fragrance, double score, List<String> reasons) {
        this.fragrance = fragrance;
        this.score     = score;
        this.reasons   = reasons;
    }

    public FragranceDto getFragrance()  { return fragrance; }
    public double getScore()            { return score; }
    public List<String> getReasons()    { return reasons; }

    public String getScorePercent() {
        return String.format("%.0f", score * 100);
    }
}
