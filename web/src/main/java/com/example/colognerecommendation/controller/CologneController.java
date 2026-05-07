package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.Occasion;
import com.example.colognerecommendation.model.Weather;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

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
}
