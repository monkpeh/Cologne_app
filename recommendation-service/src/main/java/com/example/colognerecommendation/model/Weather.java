package com.example.colognerecommendation.model;

public enum Weather {
    HOT("Hot (30°C+)"),
    WARM("Warm (20–30°C)"),
    MILD("Mild (10–20°C)"),
    COOL("Cool (5–10°C)"),
    COLD("Cold (Below 5°C)");

    private final String label;

    Weather(String label) { this.label = label; }

    public String getLabel() { return label; }
}
