package com.example.colognerecommendation.dto;

import java.io.Serializable;
import java.util.List;

public class ScoredFragranceDto implements Serializable {

    private FragranceDto fragrance;
    private double score;
    private List<String> reasons;

    public ScoredFragranceDto() {}

    public ScoredFragranceDto(FragranceDto fragrance, double score, List<String> reasons) {
        this.fragrance = fragrance;
        this.score     = score;
        this.reasons   = reasons;
    }

    public FragranceDto getFragrance()  { return fragrance; }
    public double getScore()            { return score; }
    public List<String> getReasons()    { return reasons; }

    public void setFragrance(FragranceDto fragrance) { this.fragrance = fragrance; }
    public void setScore(double score)               { this.score = score; }
    public void setReasons(List<String> reasons)     { this.reasons = reasons; }

    public String getScorePercent() {
        return String.format("%.0f", score * 100);
    }
}
