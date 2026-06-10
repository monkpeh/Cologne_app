package com.example.colognerecommendation.dto;

import java.io.Serializable;
import java.util.List;

public class RecommendationRequest implements Serializable {

    private String correlationId;
    private String username;
    private String weather;
    private String occasion;
    private List<FragranceDto> collection;

    public RecommendationRequest() {}

    public String getCorrelationId()            { return correlationId; }
    public String getUsername()                 { return username; }
    public String getWeather()                  { return weather; }
    public String getOccasion()                 { return occasion; }
    public List<FragranceDto> getCollection()   { return collection; }

    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setUsername(String username)            { this.username = username; }
    public void setWeather(String weather)              { this.weather = weather; }
    public void setOccasion(String occasion)            { this.occasion = occasion; }
    public void setCollection(List<FragranceDto> c)     { this.collection = c; }
}
