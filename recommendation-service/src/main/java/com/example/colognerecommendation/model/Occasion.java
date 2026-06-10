package com.example.colognerecommendation.model;

public enum Occasion {
    CASUAL("Casual / Everyday"),
    OFFICE("Office / Work"),
    DATE("Date Night"),
    FORMAL("Formal Event"),
    SOCIAL("Social / Party");

    private final String label;

    Occasion(String label) { this.label = label; }

    public String getLabel() { return label; }
}
