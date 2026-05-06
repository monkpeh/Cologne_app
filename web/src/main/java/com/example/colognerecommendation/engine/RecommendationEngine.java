package com.example.colognerecommendation.engine;

import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule-based scoring engine that ranks a user's fragrance collection for a given context.
 *
 * <h2>Score breakdown (all components normalised to [0.0, 1.0])</h2>
 * <pre>
 *   Total = weatherScore × 0.40
 *         + occasionScore × 0.40
 *         + (longevity / 5.0) × 0.20
 * </pre>
 *
 * <p>Weather and occasion each carry 40 % because context fit is the primary concern.
 * Longevity carries 20 % as a practical tiebreaker — a perfectly suited fragrance
 * that fades in two hours is less useful than one that lasts all day.
 *
 * <p>This class is intentionally framework-free (no Spring annotations) so it can be
 * instantiated and tested in isolation without starting an application context.
 */
public class RecommendationEngine {

    /**
     * Scores every fragrance in the collection against the given context and returns
     * the top {@code topN} results, ranked highest score first.
     *
     * @param collection the user's fragrance list; may be empty
     * @param weather    the current outdoor temperature band
     * @param occasion   the social context for wearing the fragrance
     * @param topN       maximum number of results to return
     * @return a ranked list of up to {@code topN} results, or an empty list if the collection is empty
     */
    public List<RecommendationResult> recommend(
            List<Fragrance> collection,
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

    /**
     * Convenience overload that returns the top 3 results.
     *
     * @param collection the user's fragrance list
     * @param weather    the current weather condition
     * @param occasion   the intended occasion
     * @return ranked list of up to 3 recommendations
     */
    public List<RecommendationResult> recommend(
            List<Fragrance> collection, Weather weather, Occasion occasion) {
        return recommend(collection, weather, occasion, 3);
    }

    // ── Private scoring methods ───────────────────────────────────────────────

    /**
     * Computes the composite score for a single fragrance and assembles the reasons list.
     *
     * @param f       the fragrance to evaluate
     * @param weather the weather context
     * @param occasion the occasion context
     * @return a fully populated {@link RecommendationResult}
     */
    private RecommendationResult score(Fragrance f, Weather weather, Occasion occasion) {
        double total = 0.0;
        List<String> reasons = new ArrayList<>();

        // ── Weather component (40%) ──────────────────────────────────────────
        double wScore = weatherScore(f, weather);
        total += wScore * 0.40;

        if      (wScore >= 0.80) reasons.add("Excellent choice for " + weather.getLabel() + " conditions");
        else if (wScore >= 0.55) reasons.add("Well-suited to " + weather.getLabel() + " conditions");
        else if (wScore <  0.35) reasons.add("Note: not optimised for " + weather.getLabel() + " weather");

        // ── Occasion component (40%) ─────────────────────────────────────────
        double oScore = occasionScore(f, occasion);
        total += oScore * 0.40;

        switch (occasion) {
            case OFFICE:
                reasons.add(f.officeSafe
                        ? "Office-appropriate scent strength"
                        : "Caution: may be too strong for an office environment");
                break;
            case DATE:
                if (f.projection >= 4) reasons.add("Strong projection for a memorable impression");
                break;
            case SOCIAL:
                if (f.projection >= 4) reasons.add("Great presence for social settings");
                break;
            case CASUAL:
                reasons.add("Comfortable projection for everyday wear");
                break;
            case FORMAL:
                if (f.officeSafe) reasons.add("Sophisticated and event-appropriate");
                break;
        }

        // ── Longevity component (20%) ────────────────────────────────────────
        total += (f.longevity / 5.0) * 0.20;
        if (f.longevity >= 4) reasons.add("Long-lasting – won't need a reapplication");

        if (reasons.isEmpty()) reasons.add("A solid all-around option");

        return new RecommendationResult(f, total, reasons);
    }

    /**
     * Calculates a weather suitability score in [0.0, 1.0] by blending the fragrance's
     * hot and cold season ratings according to the selected weather band.
     *
     * <p>Extreme bands (HOT / COLD) use a single season rating directly.
     * Intermediate bands (WARM, MILD, COOL) blend both ratings with complementary weights
     * so that the score transitions smoothly rather than snapping between extremes.
     *
     * @param f       the fragrance whose season ratings are used
     * @param weather the temperature band selected by the user
     * @return normalised weather score in [0.0, 1.0]
     */
    private double weatherScore(Fragrance f, Weather weather) {
        switch (weather) {
            case HOT:  return f.seasonHot / 10.0;
            case WARM: return (f.seasonHot * 0.7 + f.seasonCold * 0.3) / 10.0;
            case MILD: return (f.seasonHot * 0.5 + f.seasonCold * 0.5) / 10.0;
            case COOL: return (f.seasonHot * 0.3 + f.seasonCold * 0.7) / 10.0;
            case COLD: return f.seasonCold / 10.0;
            default:   return 0.5;
        }
    }

    /**
     * Calculates an occasion suitability score in [0.0, 1.0] using occasion-specific rules.
     *
     * <ul>
     *   <li><b>OFFICE:</b> Non-office-safe fragrances score only 0.20; projection above 3
     *       incurs an additional 0.80 multiplier to penalise overpowering scents.</li>
     *   <li><b>CASUAL:</b> Projection 3 scores perfectly (1.0); scores taper off on either side.</li>
     *   <li><b>DATE:</b> Linear combination of projection (60 %) and longevity (40 %).</li>
     *   <li><b>SOCIAL:</b> Projection-heavy weighting (70 %) with longevity support (30 %).</li>
     *   <li><b>FORMAL:</b> Balances office-appropriateness (50 %) against moderate projection (50 %).</li>
     * </ul>
     *
     * @param f       the fragrance to evaluate
     * @param occasion the occasion selected by the user
     * @return normalised occasion score in [0.0, 1.0]
     */
    private double occasionScore(Fragrance f, Occasion occasion) {
        switch (occasion) {
            case OFFICE: {
                double safety = f.officeSafe ? 1.0 : 0.20;
                double proj   = f.projection > 3 ? 0.80 : 1.0;
                return safety * proj;
            }
            case CASUAL: {
                switch (f.projection) {
                    case 1: return 0.60;
                    case 2: return 0.80;
                    case 3: return 1.00;
                    case 4: return 0.90;
                    default: return 0.70;
                }
            }
            case DATE:   return (f.projection / 5.0) * 0.60 + (f.longevity / 5.0) * 0.40;
            case SOCIAL: return (f.projection / 5.0) * 0.70 + (f.longevity / 5.0) * 0.30;
            case FORMAL: {
                double safety = f.officeSafe ? 1.0 : 0.50;
                double proj;
                switch (f.projection) {
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
