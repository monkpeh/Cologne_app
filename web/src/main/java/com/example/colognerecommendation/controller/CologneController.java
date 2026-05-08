package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the three main user-facing pages: Collection, Add, and Recommend.
 *
 * <p>Every method receives a {@link Principal} parameter which Spring Security populates
 * automatically with the currently authenticated user. Calling {@code principal.getName()}
 * returns the username, which is passed to {@link FragranceService} to scope all
 * collection operations to that user's data.
 *
 * <p>Flash attributes ({@link RedirectAttributes#addFlashAttribute}) are used to pass
 * one-time toast messages across POST → redirect → GET cycles without exposing them in the URL.
 */
@Controller
public class CologneController {

    private final FragranceService service;

    public CologneController(FragranceService service) {
        this.service = service;
    }

    // ── Root ──────────────────────────────────────────────────────────────────

    /**
     * Redirects the application root to the collection page.
     *
     * @return a redirect instruction to {@code /collection}
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/collection";
    }

    // ── Collection ────────────────────────────────────────────────────────────

    /**
     * Renders the user's personal fragrance collection with optional sorting and filtering.
     *
     * <p>Sort and filter values are passed as URL query parameters so the resulting
     * URL is bookmarkable and shareable. The active sort/filter values are added to the model
     * so the template can highlight the correct buttons.
     *
     * @param sort      field to sort by — one of {@code name} (default), {@code brand},
     *                  {@code projection}, {@code longevity}, {@code scentFamily}
     * @param filter    occasion filter — one of {@code all} (default), {@code office}, {@code casual}
     * @param principal the authenticated user injected by Spring Security
     * @param model     the Thymeleaf model
     * @return the {@code collection} template name
     */
    @GetMapping("/collection")
    public String collection(
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "all")  String filter,
            Principal principal,
            Model model) {
        String username = principal.getName();
        model.addAttribute("collection",  service.getSortedFilteredCollection(sort, filter, username));
        model.addAttribute("sort",        sort);
        model.addAttribute("filter",      filter);
        model.addAttribute("ratings",     service.getUserRatings(username));
        model.addAttribute("suggestions", service.getSuggestions(username));
        return "collection";
    }

    /**
     * Removes a fragrance from the authenticated user's collection and redirects back
     * to the collection page, preserving the current sort and filter.
     *
     * <p>A flash toast message is set so the collection page can confirm the removal
     * without requiring the message to survive in the URL.
     *
     * @param id        the database ID of the fragrance to remove (from the URL path)
     * @param sort      the active sort value — preserved in the redirect URL
     * @param filter    the active filter value — preserved in the redirect URL
     * @param principal the authenticated user
     * @param ra        flash attributes for the post-redirect toast message
     * @return a redirect to {@code /collection} with sort and filter parameters
     */
    @PostMapping("/collection/rate/{id}")
    public String rate(@PathVariable int id,
                       @RequestParam int rating,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "all")  String filter,
                       Principal principal,
                       RedirectAttributes ra) {
        service.rateFragrance(id, rating, principal.getName());
        ra.addFlashAttribute("toast", rating > 0
                ? "Rating saved — " + rating + " star" + (rating == 1 ? "" : "s") + "."
                : "Rating cleared.");
        return "redirect:/collection?sort=" + sort + "&filter=" + filter;
    }

    @PostMapping("/collection/remove/{id}")
    public String remove(@PathVariable int id,
                         @RequestParam(defaultValue = "name") String sort,
                         @RequestParam(defaultValue = "all")  String filter,
                         Principal principal,
                         RedirectAttributes ra) {
        service.removeFromCollection(id, principal.getName());
        ra.addFlashAttribute("toast", "Fragrance removed from your collection.");
        return "redirect:/collection?sort=" + sort + "&filter=" + filter;
    }

    // ── Add cologne ───────────────────────────────────────────────────────────

    /**
     * Renders the Add page showing the full fragrance catalogue with a search bar.
     *
     * <p>The user's current collection IDs are passed so the template can mark
     * already-added fragrances with a disabled "In Collection" button rather than
     * an active "Add to Collection" button.
     *
     * @param q         the search query; empty string returns the full catalogue
     * @param principal the authenticated user
     * @param model     the Thymeleaf model
     * @return the {@code add} template name
     */
    @GetMapping("/add")
    public String addPage(@RequestParam(defaultValue = "") String q,
                          Principal principal,
                          Model model) {
        model.addAttribute("fragrances",      service.searchFragrances(q));
        model.addAttribute("query",           q);
        model.addAttribute("collectionIds",   service.getCollectionIds(principal.getName()));
        model.addAttribute("averageRatings",  service.getAverageRatings());
        return "add";
    }

    /**
     * Adds a fragrance to the authenticated user's collection and redirects back
     * to the Add page, restoring the previous search query.
     *
     * @param id          the database ID of the fragrance to add (from the URL path)
     * @param returnQuery the search query that was active — appended to the redirect URL
     * @param principal   the authenticated user
     * @param ra          flash attributes for the post-redirect toast message
     * @return a redirect to {@code /add} with the original query restored
     */
    @PostMapping("/collection/add/{id}")
    public String add(@PathVariable int id,
                      @RequestParam(defaultValue = "") String returnQuery,
                      Principal principal,
                      RedirectAttributes ra) {
        service.addToCollection(id, principal.getName());
        ra.addFlashAttribute("toast", "Added to your collection!");
        return "redirect:/add?q=" + returnQuery;
    }

    // ── Recommendation ────────────────────────────────────────────────────────

    /**
     * Renders the recommendation form page.
     *
     * <p>All {@link Weather} and {@link Occasion} enum values are passed to populate
     * the dropdowns. The {@code collectionEmpty} flag disables the submit button when
     * the user has no fragrances to recommend from.
     *
     * @param principal the authenticated user
     * @param model     the Thymeleaf model
     * @return the {@code recommend} template name
     */
    @GetMapping("/recommend")
    public String recommendPage(Principal principal, Model model) {
        model.addAttribute("weathers",        Weather.values());
        model.addAttribute("occasions",       Occasion.values());
        model.addAttribute("collectionEmpty", service.getUserCollection(principal.getName()).isEmpty());
        return "recommend";
    }

    /**
     * Processes the recommendation form and renders ranked results on the same page.
     *
     * <p>The selected weather and occasion values are parsed back to their enum types,
     * passed to the service which delegates to {@link com.example.colognerecommendation.engine.RecommendationEngine},
     * and the ranked results are added to the model alongside the form inputs so the
     * dropdowns remember the user's last selection.
     *
     * @param weather   the weather enum constant name submitted by the form
     * @param occasion  the occasion enum constant name submitted by the form
     * @param principal the authenticated user
     * @param model     the Thymeleaf model
     * @return the {@code recommend} template name (same page, now with results)
     */
    @PostMapping("/recommend")
    public String recommend(@RequestParam String weather,
                            @RequestParam String occasion,
                            Principal principal,
                            Model model) {
        Weather  w = Weather.valueOf(weather);
        Occasion o = Occasion.valueOf(occasion);

        model.addAttribute("results",          service.getRecommendations(w, o, principal.getName()));
        model.addAttribute("weathers",         Weather.values());
        model.addAttribute("occasions",        Occasion.values());
        model.addAttribute("selectedWeather",  weather);
        model.addAttribute("selectedOccasion", occasion);
        model.addAttribute("collectionEmpty",  service.getUserCollection(principal.getName()).isEmpty());
        return "recommend";
    }

    // ── Stats dashboard ───────────────────────────────────────────────────────

    /**
     * Renders the personal stats dashboard.
     *
     * <p><b>How it works (MVC flow):</b>
     * <ol>
     *   <li>Spring calls this method when the browser requests GET /stats</li>
     *   <li>{@code principal.getName()} returns the logged-in username</li>
     *   <li>The service crunches the numbers and returns a {@code UserStats} object</li>
     *   <li>{@code model.addAttribute("stats", ...)} places it in a map keyed "stats"</li>
     *   <li>Thymeleaf reads {@code ${stats.totalOwned}} by calling {@code getTotalOwned()}</li>
     * </ol>
     *
     * <p>We also add the raw {@code ratings} map separately because the top-rated section
     * in the template needs to render star icons per fragrance, not just the computed count.
     */
    @GetMapping("/stats")
    public String stats(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("stats",   service.getStats(username));
        model.addAttribute("ratings", service.getUserRatings(username));
        return "stats";
    }

    // ── Side-by-side comparison ───────────────────────────────────────────────

    /**
     * Renders the side-by-side fragrance comparison page.
     *
     * <p><b>How {@code @RequestParam List<Integer> ids} works:</b>
     * A URL like {@code /compare?ids=1&ids=2&ids=3} contains three parameters all
     * named "ids". Spring collects them into a {@code List<Integer>}, converting each
     * string to an int automatically. {@code required = false} prevents a 400 error
     * if the user navigates to /compare with no params.
     *
     * <p>Security: only fragrances the user actually owns are resolved. This prevents
     * someone from crafting a URL with arbitrary IDs they don't own.
     */
    // ── Submit a fragrance (user-facing) ─────────────────────────────────────

    /**
     * Renders the user-facing "Submit a Fragrance" form.
     * Any authenticated user can access this — no ADMIN role required.
     */
    @GetMapping("/fragrances/new")
    public String newFragranceForm(Model model) {
        model.addAttribute("fragrance", new Fragrance());
        return "fragrance_new";
    }

    /**
     * Processes the user-submitted fragrance, saves it to the catalogue,
     * automatically adds it to the submitter's collection, and syncs fragrances.json.
     *
     * <p>After saving, the new fragrance is added straight to the user's collection
     * so they can rate and compare it immediately without a separate step.
     */
    @PostMapping("/fragrances/new")
    public String submitFragrance(@RequestParam String  brand,
                                  @RequestParam String  name,
                                  @RequestParam String  scentFamily,
                                  @RequestParam int     projection,
                                  @RequestParam int     longevity,
                                  @RequestParam int     seasonHot,
                                  @RequestParam int     seasonCold,
                                  @RequestParam(defaultValue = "false") boolean officeSafe,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) String imageUrl,
                                  Principal principal,
                                  RedirectAttributes ra) {
        Fragrance f = new Fragrance();
        f.brand = brand; f.name = name; f.scentFamily = scentFamily;
        f.projection = projection; f.longevity = longevity;
        f.seasonHot = seasonHot; f.seasonCold = seasonCold;
        f.officeSafe = officeSafe; f.description = description;
        f.imageUrl = (imageUrl != null && imageUrl.isBlank()) ? null : imageUrl;
        service.saveFragrance(f);
        service.syncToJson();
        // Auto-add to the submitter's collection so it's ready to rate/compare
        service.addToCollection(f.id, principal.getName());
        ra.addFlashAttribute("toast",
                "'" + f.name + "' added to the catalogue and your collection!");
        return "redirect:/add";
    }

    // ── Side-by-side comparison ───────────────────────────────────────────────

    /**
     * Renders the side-by-side fragrance comparison page.
     *
     * <p><b>How {@code @RequestParam List<Integer> ids} works:</b>
     * A URL like {@code /compare?ids=1&ids=2&ids=3} contains three parameters all
     * named "ids". Spring collects them into a {@code List<Integer>}, converting each
     * string to an int automatically. {@code required = false} prevents a 400 error
     * if the user navigates to /compare with no params.
     *
     * <p>Security: only fragrances the user actually owns are resolved. This prevents
     * someone from crafting a URL with arbitrary IDs they don't own.
     */
    @GetMapping("/compare")
    public String compare(
            @RequestParam(required = false) List<Integer> ids,
            Principal principal,
            Model model) {

        if (ids == null || ids.size() < 2) return "redirect:/collection";

        String username = principal.getName();
        Set<Integer> collectionIds = service.getCollectionIds(username);

        List<Fragrance> fragrances = ids.stream()
                .limit(3)
                .filter(collectionIds::contains)
                .map(service::findFragranceById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (fragrances.size() < 2) return "redirect:/collection";

        model.addAttribute("fragrances", fragrances);
        model.addAttribute("ratings",    service.getUserRatings(username));
        return "compare";
    }
}