package com.example.colognerecommendation.dto;

import java.io.Serializable;
import java.util.List;

public class RecommendationResponse implements Serializable {

    private String correlationId;
    private List<ScoredFragranceDto> results;

    public RecommendationResponse() {}

    public String getCorrelationId()             { return correlationId; }
    public List<ScoredFragranceDto> getResults() { return results; }

    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setResults(List<ScoredFragranceDto> r) { this.results = r; }
}
