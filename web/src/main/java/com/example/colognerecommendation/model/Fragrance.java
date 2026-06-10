package com.example.colognerecommendation.model;

import jakarta.validation.constraints.*;
import jakarta.persistence.*;

/**
 * Represents a single fragrance entry in the catalogue.
 *
 * <p>This class serves two roles simultaneously:
 * <ol>
 *   <li><b>JPA entity</b> – persisted to the {@code fragrance} database table.
 *       Public fields are used so Hibernate can access them via field-level reflection
 *       without requiring explicit column annotations on every field.</li>
 *   <li><b>Thymeleaf view model</b> – getters follow the JavaBean convention so that
 *       Thymeleaf's Spring Expression Language (SpEL) can evaluate {@code ${f.brand}},
 *       {@code ${f.projection}}, etc. in HTML templates.</li>
 * </ol>
 *
 * <p>On first startup the table is seeded from {@code fragrances.json} by
 * {@link com.example.colognerecommendation.service.FragranceService#seedDataset()}.
 * Admins can add, edit, and delete fragrances at runtime via the admin panel.
 */
@Entity
@Table(name = "fragrance")
public class Fragrance {

    /** Auto-generated primary key assigned by the H2 IDENTITY column. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    /** Manufacturer or fashion house name (e.g. "Dior"). */
    @NotBlank(message = "Brand cannot be empty")
    public String brand;

    /** Commercial name of the fragrance (e.g. "Sauvage"). */
    @NotBlank(message = "Name cannot be empty")
    public String name;

    /** Olfactive family classification (e.g. "Fresh / Spicy", "Oriental / Woody"). */
    @NotBlank(message = "Scent family cannot be empty")
    public String scentFamily;

    /**
     * How far the scent projects from the skin, rated 1 (skin-close) to 5 (beast mode).
     * Used by the recommendation engine to evaluate office and social suitability.
     */
    @Min(value = 1, message = "Projection must be between 1 and 5")
    @Max(value = 5, message = "Projection must be between 1 and 5")
    public int projection;

    /**
     * How many hours the scent lasts before fading significantly, rated 1 (2–3 h) to 5 (12 h+).
     * Contributes 20 % of the total recommendation score as a practical tiebreaker.
     */
    @Min(value = 1, message = "Longevity must be between 1 and 5")
    @Max(value = 5, message = "Longevity must be between 1 and 5")
    public int longevity;

    /**
     * Suitability for hot weather on a scale of 0 (avoid) to 10 (perfect).
     * Used directly for HOT weather scoring and blended for WARM and MILD.
     */
    @Min(value = 0, message = "Season hot must be between 0 and 10")
    @Max(value = 10, message = "Season hot must be between 0 and 10")
    public int seasonHot;

    /**
     * Suitability for cold weather on a scale of 0 (avoid) to 10 (perfect).
     * Used directly for COLD weather scoring and blended for COOL and MILD.
     */
    @Min(value = 0, message = "Season cold must be between 0 and 10")
    @Max(value = 10, message = "Season cold must be between 0 and 10")
    public int seasonCold;

    /**
     * Whether this fragrance is considered appropriate for a professional office environment.
     * Typically true for light, low-projection, inoffensive scents.
     */

    public boolean officeSafe;

    /** Short description of the fragrance's character and notable notes. */
    @Column(length = 2000)
    public String description;

    /**
     * Optional URL pointing to a bottle image.
     * When set, the image is displayed at the top of the fragrance card across all pages.
     * When null or blank, a droplet placeholder icon is shown instead.
     */
    @Column(length = 1000)
    public String imageUrl;

    // ── Getters (required by Thymeleaf SpEL) ─────────────────────────────────

    /** @return the database primary key */
    public Integer getId()          { return id; }

    /** @return the brand / manufacturer name */
    public String  getBrand()       { return brand; }

    /** @return the commercial fragrance name */
    public String  getName()        { return name; }

    /** @return the olfactive family classification */
    public String  getScentFamily() { return scentFamily; }

    /** @return projection rating 1–5 */
    public int     getProjection()  { return projection; }

    /** @return longevity rating 1–5 */
    public int     getLongevity()   { return longevity; }

    /** @return hot-weather suitability 0–10 */
    public int     getSeasonHot()   { return seasonHot; }

    /** @return cold-weather suitability 0–10 */
    public int     getSeasonCold()  { return seasonCold; }

    /** @return {@code true} if appropriate for office use */
    public boolean isOfficeSafe()   { return officeSafe; }

    /** @return the short descriptive text, or {@code null} if not set */
    public String  getDescription() { return description; }

    /** @return the bottle image URL, or {@code null} if no image has been set */
    public String  getImageUrl()    { return imageUrl; }
}
