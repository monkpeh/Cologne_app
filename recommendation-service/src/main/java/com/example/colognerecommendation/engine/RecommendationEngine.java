package com.example.colognerecommendation.engine;

import com.example.colognerecommendation.dto.FragranceDto;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationEngine {

    public List<RecommendationResult> recommend(
            List<FragranceDto> collection,
            Weather weather,
            Occasion occasion,
            int topN) {

        if (collection.isEmpty()) return Collections.emptyList();

        return collection.stream()
                .map(f -> score(f, weather, occasion))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    public List<RecommendationResult> recommend(
            List<FragranceDto> collection, Weather weather, Occasion occasion) {
        return recommend(collection, weather, occasion, 3);
    }

    private RecommendationResult score(FragranceDto f, Weather weather, Occasion occasion) {
        double total = 0.0;
        List<String> reasons = new ArrayList<>();

        double wScore = weatherScore(f, weather);
        total += wScore * 0.40;

        if      (wScore >= 0.80) reasons.add("Excellent choice for " + weather.getLabel() + " conditions");
        else if (wScore >= 0.55) reasons.add("Well-suited to " + weather.getLabel() + " conditions");
        else if (wScore <  0.35) reasons.add("Note: not optimised for " + weather.getLabel() + " weather");

        double oScore = occasionScore(f, occasion);
        total += oScore * 0.40;

        switch (occasion) {
            case OFFICE:
                reasons.add(f.isOfficeSafe()
                        ? "Office-appropriate scent strength"
                        : "Caution: may be too strong for an office environment");
                break;
            case DATE:
                if (f.getProjection() >= 4) reasons.add("Strong projection for a memorable impression");
                break;
            case SOCIAL:
                if (f.getProjection() >= 4) reasons.add("Great presence for social settings");
                break;
            case CASUAL:
                reasons.add("Comfortable projection for everyday wear");
                break;
            case FORMAL:
                if (f.isOfficeSafe()) reasons.add("Sophisticated and event-appropriate");
                break;
        }

        total += (f.getLongevity() / 5.0) * 0.20;
        if (f.getLongevity() >= 4) reasons.add("Long-lasting – won't need a reapplication");

        if (reasons.isEmpty()) reasons.add("A solid all-around option");

        return new RecommendationResult(f, total, reasons);
    }

    private double weatherScore(FragranceDto f, Weather weather) {
        switch (weather) {
            case HOT:  return f.getSeasonHot() / 10.0;
            case WARM: return (f.getSeasonHot() * 0.7 + f.getSeasonCold() * 0.3) / 10.0;
            case MILD: return (f.getSeasonHot() * 0.5 + f.getSeasonCold() * 0.5) / 10.0;
            case COOL: return (f.getSeasonHot() * 0.3 + f.getSeasonCold() * 0.7) / 10.0;
            case COLD: return f.getSeasonCold() / 10.0;
            default:   return 0.5;
        }
    }

    private double occasionScore(FragranceDto f, Occasion occasion) {
        switch (occasion) {
            case OFFICE: {
                double safety = f.isOfficeSafe() ? 1.0 : 0.20;
                double proj   = f.getProjection() > 3 ? 0.80 : 1.0;
                return safety * proj;
            }
            case CASUAL: {
                switch (f.getProjection()) {
                    case 1: return 0.60;
                    case 2: return 0.80;
                    case 3: return 1.00;
                    case 4: return 0.90;
                    default: return 0.70;
                }
            }
            case DATE:   return (f.getProjection() / 5.0) * 0.60 + (f.getLongevity() / 5.0) * 0.40;
            case SOCIAL: return (f.getProjection() / 5.0) * 0.70 + (f.getLongevity() / 5.0) * 0.30;
            case FORMAL: {
                double safety = f.isOfficeSafe() ? 1.0 : 0.50;
                double proj;
                switch (f.getProjection()) {
                    case 1: proj = 0.50; break;
                    case 2: proj = 0.80; break;
                    case 3: proj = 1.00; break;
                    case 4: proj = 0.90; break;
                    default: proj = 0.70; break;
                }
                return safety * 0.50 + proj * 0.50;
            }
            default: return 0.5;
        }
    }
}
