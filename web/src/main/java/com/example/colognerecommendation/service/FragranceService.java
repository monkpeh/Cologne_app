package com.example.colognerecommendation.service;

import com.example.colognerecommendation.engine.RecommendationEngine;
import com.example.colognerecommendation.engine.RecommendationResult;
import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.UserStats;
import com.example.colognerecommendation.model.Weather;
import com.example.colognerecommendation.repository.FragranceRepository;
import com.example.colognerecommendation.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central business-logic service for the Cologne Advisor application.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li><b>Catalogue seeding</b> – on first startup, reads {@code fragrances.json} from the
 *       classpath and populates the {@code fragrance} database table if it is empty.</li>
 *   <li><b>Catalogue queries</b> – exposes all fragrances and a keyword search over brand,
 *       name, and scent family.</li>
 *   <li><b>Fragrance CRUD</b> – save and delete fragrances on behalf of the admin panel.</li>
 *   <li><b>User collection management</b> – adds, removes, and retrieves the per-user
 *       fragrance list with optional sorting and filtering.</li>
 *   <li><b>Recommendations</b> – delegates to {@link RecommendationEngine} after fetching
 *       the user's collection.</li>
 * </ol>
 *
 * <p>All collection operations are scoped by {@code username} (the authenticated principal's name)
 * so each user's data remains fully isolated from other users'.
 */
@Service
public class FragranceService {

    private final RecommendationEngine engine = new RecommendationEngine();
    private final FragranceRepository  fragranceRepository;
    private final UserRepository       userRepository;

    @Value("${app.fragrances.json-path}")
    private String fragrancesJsonPath;

    public FragranceService(FragranceRepository fragranceRepository,
                            UserRepository userRepository) {
        this.fragranceRepository = fragranceRepository;
        this.userRepository      = userRepository;
    }

    // ── Seeding ───────────────────────────────────────────────────────────────

    /**
     * Populates the {@code fragrance} table from {@code fragrances.json} on application startup.
     *
     * <p>The method is idempotent: if any rows already exist in the table it exits immediately,
     * so the seed data is never duplicated across restarts.
     *
     * <p>Before saving, the fragrances are sorted by their original JSON {@code id} value and
     * then that ID is nulled out. This ensures that H2's IDENTITY column assigns IDs 1, 2, 3 …
     * in the same order as the JSON file, keeping existing {@code user_collection} references valid.
     *
     * @throws IllegalStateException if the JSON file cannot be found or parsed
     */
    @PostConstruct
    public void seedDataset() {
        if (fragranceRepository.count() > 0) return;

        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/fragrances.json"),
                        "fragrances.json not found on classpath"))) {

            Type type = new TypeToken<List<Fragrance>>() {}.getType();
            List<Fragrance> loaded = new Gson().fromJson(reader, type);

            loaded.sort(Comparator.comparingInt(f -> (f.id != null ? f.id : 0)));
            loaded.forEach(f -> f.id = null);

            fragranceRepository.saveAll(loaded);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed fragrance dataset", e);
        }
    }

    // ── Catalogue queries ─────────────────────────────────────────────────────

    /**
     * Returns the complete fragrance catalogue in database insertion order.
     *
     * @return an unordered list of all fragrances currently in the database
     */
    public List<Fragrance> getAllFragrances() {
        return fragranceRepository.findAll();
    }

    /**
     * Returns all fragrances whose brand, name, or scent family contains the query string
     * (case-insensitive). Returns the full catalogue when the query is blank.
     *
     * @param query the search term entered by the user; may be null or blank
     * @return matching fragrances, or all fragrances if the query is empty
     */
    public List<Fragrance> searchFragrances(String query) {
        if (query == null || query.isBlank()) return getAllFragrances();
        String q = query.toLowerCase().trim();
        return fragranceRepository.findAll().stream()
                .filter(f -> f.brand.toLowerCase().contains(q)
                        || f.name.toLowerCase().contains(q)
                        || f.scentFamily.toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ── Fragrance CRUD (admin) ────────────────────────────────────────────────

    /**
     * Retrieves a single fragrance by its primary key.
     *
     * @param id the fragrance's database ID
     * @return an {@link Optional} containing the fragrance, or empty if the ID does not exist
     */
    public Optional<Fragrance> findFragranceById(int id) {
        return fragranceRepository.findById(id);
    }

    /**
     * Persists a fragrance to the database. Inserts a new row when the fragrance's ID is
     * {@code null}; updates the existing row when the ID is set.
     *
     * @param fragrance the fragrance to save; its {@code id} field is updated after insert
     */
    public void saveFragrance(Fragrance fragrance) {
        fragranceRepository.save(fragrance);
    }

    /**
     * Deletes a fragrance from the catalogue and removes its ID from every user's collection.
     *
     * <p>Both operations run inside a single transaction so a partial failure cannot leave
     * orphaned IDs in the {@code user_collection} table.
     *
     * @param id the primary key of the fragrance to delete
     */
    @Transactional
    public void deleteFragrance(int id) {
        userRepository.removeFragranceFromAllCollections(id);
        userRepository.removeFragranceFromAllRatings(id);
        fragranceRepository.deleteById(id);
    }

    // ── JSON sync ─────────────────────────────────────────────────────────────

    /**
     * Re-serialises the full fragrance catalogue from the database to
     * {@code fragrances.json}, keeping the seed file in sync with runtime edits.
     *
     * <p>Called automatically after every create, update, or delete so the JSON
     * always reflects the current catalogue. If the write fails (e.g. the app is
     * running from inside a JAR where classpath resources are not writable), the
     * error is printed but not thrown — the database operation has already
     * committed successfully and we don't want to roll it back for a file-sync issue.
     *
     * <p>Gson omits null fields by default, so fragrances that have no
     * {@code imageUrl} or {@code description} will serialize cleanly without
     * {@code "imageUrl": null} noise in the JSON.
     */
    public void syncToJson() {
        List<Fragrance> all = getAllFragrances();
        all.sort(Comparator.comparingInt(f -> (f.id != null ? f.id : 0)));
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(all);

        // Write to src/main/resources so changes survive Maven rebuilds
        writeJson(new File(fragrancesJsonPath), json);

        // Also write to target/classes so the running app reads the updated file
        // on next startup without requiring a separate Maven build step
        try {
            File classpathFile = new org.springframework.core.io.ClassPathResource("fragrances.json").getFile();
            writeJson(classpathFile, json);
        } catch (IOException e) {
            System.err.println("Warning: could not sync target fragrances.json: " + e.getMessage());
        }
    }

    private void writeJson(File file, String json) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            System.err.println("Warning: could not write " + file.getPath() + ": " + e.getMessage());
        }
    }

    // ── User collection ───────────────────────────────────────────────────────

    /**
     * Retrieves the {@link AppUser} for the given username, throwing if not found.
     * Private helper used by all collection methods.
     *
     * @param username the authenticated principal's username
     * @return the corresponding {@link AppUser} entity
     * @throws IllegalStateException if no account exists for the given username
     */
    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    /**
     * Returns the full list of fragrances in the specified user's collection,
     * loaded from the database in a single {@code WHERE id IN (...)} query.
     *
     * @param username the authenticated principal's username
     * @return the user's collected fragrances, or an empty list if the collection is empty
     */
    public List<Fragrance> getUserCollection(String username) {
        Set<Integer> ids = getUser(username).getCollectionIds();
        if (ids.isEmpty()) return Collections.emptyList();
        return fragranceRepository.findAllById(ids);
    }

    /**
     * Returns the user's collection with optional filtering by occasion type and
     * sorted by the specified field.
     *
     * <p><b>Filter values:</b>
     * <ul>
     *   <li>{@code "office"} – keep only fragrances where {@code officeSafe == true}</li>
     *   <li>{@code "casual"} – keep only fragrances where {@code officeSafe == false}</li>
     *   <li>{@code "all"} (default) – no filtering applied</li>
     * </ul>
     *
     * <p><b>Sort values:</b> {@code "name"} (default), {@code "brand"}, {@code "scentFamily"}
     * sort alphabetically ascending. {@code "projection"} and {@code "longevity"} sort
     * numerically descending (highest first).
     *
     * @param sort     the field to sort by
     * @param filter   the occasion filter to apply
     * @param username the authenticated principal's username
     * @return the filtered and sorted collection; may be empty
     */
    public List<Fragrance> getSortedFilteredCollection(String sort, String filter, String username) {
        List<Fragrance> list = getUserCollection(username);

        if ("office".equals(filter)) {
            list = list.stream().filter(f -> f.officeSafe).collect(Collectors.toList());
        } else if ("casual".equals(filter)) {
            list = list.stream().filter(f -> !f.officeSafe).collect(Collectors.toList());
        }

        Comparator<Fragrance> comparator;
        switch (sort) {
            case "brand":
                comparator = Comparator.comparing(f -> f.brand);
                break;
            case "projection":
                comparator = Comparator.comparingInt((Fragrance f) -> f.projection).reversed();
                break;
            case "longevity":
                comparator = Comparator.comparingInt((Fragrance f) -> f.longevity).reversed();
                break;
            case "scentFamily":
                comparator = Comparator.comparing(f -> f.scentFamily);
                break;
            default:
                comparator = Comparator.comparing(f -> f.name);
                break;
        }

        list.sort(comparator);
        return list;
    }

    /**
     * Returns an unmodifiable view of the fragrance IDs in the user's collection.
     * Used by the Add page to determine which catalogue cards should show "In Collection".
     *
     * @param username the authenticated principal's username
     * @return an unmodifiable set of fragrance IDs
     */
    public Set<Integer> getCollectionIds(String username) {
        return Collections.unmodifiableSet(getUser(username).getCollectionIds());
    }

    /**
     * Adds a fragrance to the specified user's collection and persists the change.
     * Does nothing if the fragrance is already in the collection (Set semantics).
     *
     * @param fragranceId the ID of the fragrance to add
     * @param username    the authenticated principal's username
     */
    @Transactional
    public void addToCollection(int fragranceId, String username) {
        AppUser user = getUser(username);
        user.getCollectionIds().add(fragranceId);
        userRepository.save(user);
    }

    /**
     * Removes a fragrance from the specified user's collection and persists the change.
     * Does nothing if the fragrance was not in the collection.
     *
     * @param fragranceId the ID of the fragrance to remove
     * @param username    the authenticated principal's username
     */
    @Transactional
    public void removeFromCollection(int fragranceId, String username) {
        AppUser user = getUser(username);
        user.getCollectionIds().remove(fragranceId);
        userRepository.save(user);
    }

    // ── Ratings ───────────────────────────────────────────────────────────────

    /**
     * Saves or removes a user's star rating for a fragrance they own.
     * A {@code rating} of 0 removes any existing rating.
     *
     * @param fragranceId the fragrance to rate
     * @param rating      star value 1–5, or 0 to clear
     * @param username    the authenticated principal's username
     */
    @Transactional
    public void rateFragrance(int fragranceId, int rating, String username) {
        AppUser user = getUser(username);
        if (rating <= 0) {
            user.getRatings().remove(fragranceId);
        } else {
            user.getRatings().put(fragranceId, Math.min(5, rating));
        }
        userRepository.save(user);
    }

    /**
     * Returns the calling user's fragrance-ID → rating map.
     *
     * @param username the authenticated principal's username
     * @return unmodifiable map of fragrance ID to star rating (1–5)
     */
    public Map<Integer, Integer> getUserRatings(String username) {
        return Collections.unmodifiableMap(getUser(username).getRatings());
    }

    /**
     * Computes the average rating for every fragrance that has been rated by at
     * least one user. All user records are scanned once and aggregated in Java.
     *
     * @return map of fragrance ID to average star rating (1.0–5.0)
     */
    public Map<Integer, Double> getAverageRatings() {
        Map<Integer, List<Integer>> bucket = new HashMap<>();
        for (AppUser user : userRepository.findAll()) {
            user.getRatings().forEach((fid, r) ->
                    bucket.computeIfAbsent(fid, k -> new ArrayList<>()).add(r));
        }
        Map<Integer, Double> averages = new HashMap<>();
        bucket.forEach((fid, list) ->
                averages.put(fid, list.stream().mapToInt(Integer::intValue).average().orElse(0)));
        return averages;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    /**
     * Computes all dashboard statistics for the given user's collection.
     *
     * <p><b>How to read this code:</b> Every calculation is a Java stream pipeline.
     * A stream is like an assembly line: start with a collection, apply filter/map/sort
     * steps, and collect the result. No extra database queries are made here —
     * we reuse {@link #getUserCollection} and {@link #getUserRatings}.
     *
     * @param username the authenticated principal's username
     * @return a fully populated {@link UserStats} view model ready for the template
     */
    public UserStats getStats(String username) {
        List<Fragrance>       collection = getUserCollection(username);
        Map<Integer, Integer> ratings    = getUserRatings(username);

        int totalOwned = collection.size();

        // Only count ratings whose fragrance ID is still in the collection
        List<Integer> ratingValues = collection.stream()
                .filter(f -> ratings.containsKey(f.id))
                .map(f -> ratings.get(f.id))
                .collect(Collectors.toList());

        int    totalRated    = ratingValues.size();
        // mapToInt + average() returns OptionalDouble; orElse(0.0) handles empty collection
        double averageRating = ratingValues.stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);

        // groupingBy partitions the list into buckets keyed by scent family name;
        // counting() produces a Long count for each bucket → Map<String, Long>
        Map<String, Long> familyCounts = collection.stream()
                .collect(Collectors.groupingBy(f -> f.scentFamily, Collectors.counting()));

        long officeSafeCount = collection.stream().filter(f ->  f.officeSafe).count();
        long casualCount     = collection.stream().filter(f -> !f.officeSafe).count();

        // Fragrances the user has rated 4 or 5 stars, best first
        List<Fragrance> topRated = collection.stream()
                .filter(f -> ratings.getOrDefault(f.id, 0) >= 4)
                .sorted(Comparator.comparingInt(
                        (Fragrance f) -> ratings.getOrDefault(f.id, 0)).reversed())
                .collect(Collectors.toList());

        // max() returns Optional because the stream might be empty (empty collection)
        Fragrance mostProjecting = collection.stream()
                .max(Comparator.comparingInt(f -> f.projection)).orElse(null);
        Fragrance longestLasting = collection.stream()
                .max(Comparator.comparingInt(f -> f.longevity)).orElse(null);

        return new UserStats(totalOwned, totalRated, averageRating, familyCounts,
                (int) officeSafeCount, (int) casualCount,
                topRated, mostProjecting, longestLasting);
    }

    // ── Suggestions ───────────────────────────────────────────────────────────

    /**
     * Suggests up to 5 uncollected fragrances that share a scent family with
     * something already in the user's collection and have a similar season profile.
     * Season similarity is measured as the combined absolute difference between
     * the candidate's hot/cold scores and the collection's average hot/cold scores.
     * Pure Java — no additional data model is required.
     *
     * @param username the authenticated principal's username
     * @return up to 5 suggested fragrances, closest season match first
     */
    public List<Fragrance> getSuggestions(String username) {
        Set<Integer>   ownedIds = getCollectionIds(username);
        List<Fragrance> owned   = getUserCollection(username);
        if (owned.isEmpty()) return Collections.emptyList();

        Set<String> ownedFamilies = owned.stream()
                .map(f -> f.scentFamily)
                .collect(Collectors.toSet());

        double avgHot  = owned.stream().mapToInt(f -> f.seasonHot).average().orElse(5.0);
        double avgCold = owned.stream().mapToInt(f -> f.seasonCold).average().orElse(5.0);

        return fragranceRepository.findAll().stream()
                .filter(f -> !ownedIds.contains(f.id))
                .filter(f -> ownedFamilies.contains(f.scentFamily))
                .sorted(Comparator.comparingDouble(f ->
                        Math.abs(f.seasonHot - avgHot) + Math.abs(f.seasonCold - avgCold)))
                .limit(5)
                .collect(Collectors.toList());
    }

    // ── Recommendation ────────────────────────────────────────────────────────

    /**
     * Fetches the user's collection and delegates to {@link RecommendationEngine}
     * to produce a ranked list of up to 3 recommendations for the given context.
     * Results are then personalized using the user's own star ratings: a fragrance
     * rated 4–5 stars gets a small score boost; 1–2 stars gets a mild penalty.
     * The blend is 85 % engine score + 15 % user preference signal.
     *
     * @param weather  the current outdoor temperature band
     * @param occasion the intended social context
     * @param username the authenticated principal's username
     * @return ranked list of up to 3 {@link RecommendationResult} objects; empty if collection is empty
     */
    public List<RecommendationResult> getRecommendations(Weather weather, Occasion occasion, String username) {
        List<Fragrance> collection = getUserCollection(username);
        List<RecommendationResult> results = engine.recommend(collection, weather, occasion, collection.size());
        Map<Integer, Integer> ratings = getUserRatings(username);

        if (!ratings.isEmpty()) {
            results = results.stream()
                    .map(r -> {
                        Integer userRating = ratings.get(r.getFragrance().getId());
                        if (userRating == null) return r;
                        double personalized = r.getScore() * 0.85 + (userRating / 5.0) * 0.15;
                        List<String> reasons = new ArrayList<>(r.getReasons());
                        if (userRating >= 4) reasons.add("One of your highly rated fragrances");
                        return new RecommendationResult(r.getFragrance(), personalized, reasons);
                    })
                    .sorted(Comparator.comparingDouble(RecommendationResult::getScore).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
        } else {
            results = results.stream().limit(3).collect(Collectors.toList());
        }

        return results;
    }
}