package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.model.Fragrance;
import com.example.colognerecommendation.repository.UserRepository;
import com.example.colognerecommendation.service.FragranceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

/**
 * Handles all admin-only pages under the {@code /admin} path prefix.
 *
 * <p>Access is restricted to users with the {@code ADMIN} role by the Spring Security
 * filter chain configured in {@link com.example.colognerecommendation.config.SecurityConfig}.
 * Any attempt by a regular user to reach these URLs results in a 403 Forbidden response.
 *
 * <p>This controller manages two separate concerns:
 * <ul>
 *   <li><b>User management</b> – view accounts, reset passwords, toggle roles, delete accounts.</li>
 *   <li><b>Fragrance management</b> – add, edit, and delete fragrances in the catalogue.</li>
 * </ul>
 *
 * <p>Safety guards prevent an admin from deleting their own account or demoting their own role,
 * which could result in being permanently locked out of the admin panel.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FragranceService fragranceService;

    public AdminController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           FragranceService fragranceService) {
        this.userRepository   = userRepository;
        this.passwordEncoder  = passwordEncoder;
        this.fragranceService = fragranceService;
    }

    // ── User management ───────────────────────────────────────────────────────

    /**
     * Renders the user management page listing all registered accounts.
     *
     * @param model the Thymeleaf model
     * @return the {@code admin} template name
     */
    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin";
    }

    /**
     * Deletes a user account by its database ID.
     *
     * <p>An admin cannot delete their own account — if the target user's username matches
     * the current principal's name, the action is rejected with an error flash message.
     *
     * @param id        the database ID of the account to delete
     * @param principal the currently authenticated admin
     * @param ra        flash attributes for the post-redirect toast or error message
     * @return a redirect to {@code /admin}
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Optional<AppUser> opt = userRepository.findById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("errorToast", "User not found."); return "redirect:/admin"; }
        AppUser target = opt.get();
        if (target.getUsername().equals(principal.getName())) {
            ra.addFlashAttribute("errorToast", "You cannot delete your own account.");
            return "redirect:/admin";
        }
        userRepository.delete(target);
        ra.addFlashAttribute("toast", "Account '" + target.getUsername() + "' deleted.");
        return "redirect:/admin";
    }

    /**
     * Resets a user's password to a new admin-supplied value.
     *
     * <p>The new password is validated (minimum 6 characters) and BCrypt-encoded before
     * being persisted. The plain-text value is never stored.
     *
     * @param id          the database ID of the account whose password should be reset
     * @param newPassword the new plain-text password (encoded before saving)
     * @param ra          flash attributes for the post-redirect message
     * @return a redirect to {@code /admin}
     */
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes ra) {
        if (newPassword.length() < 6) { ra.addFlashAttribute("errorToast", "Password must be at least 6 characters."); return "redirect:/admin"; }
        Optional<AppUser> opt = userRepository.findById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("errorToast", "User not found."); return "redirect:/admin"; }
        AppUser target = opt.get();
        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        ra.addFlashAttribute("toast", "Password reset for '" + target.getUsername() + "'.");
        return "redirect:/admin";
    }

    /**
     * Toggles a user's role between {@code "USER"} and {@code "ADMIN"}.
     *
     * <p>An admin cannot change their own role — this prevents accidentally demoting
     * the only admin account and losing access to the admin panel entirely.
     *
     * @param id        the database ID of the account whose role should be toggled
     * @param principal the currently authenticated admin
     * @param ra        flash attributes for the post-redirect message
     * @return a redirect to {@code /admin}
     */
    @PostMapping("/toggle-role/{id}")
    public String toggleRole(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Optional<AppUser> opt = userRepository.findById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("errorToast", "User not found."); return "redirect:/admin"; }
        AppUser target = opt.get();
        if (target.getUsername().equals(principal.getName())) {
            ra.addFlashAttribute("errorToast", "You cannot change your own role.");
            return "redirect:/admin";
        }
        target.setRole("ADMIN".equals(target.getRole()) ? "USER" : "ADMIN");
        userRepository.save(target);
        ra.addFlashAttribute("toast", "Role updated for '" + target.getUsername() + "'.");
        return "redirect:/admin";
    }

    // ── Fragrance management ──────────────────────────────────────────────────

    /**
     * Renders the fragrance catalogue management page showing all fragrances in a table
     * with thumbnail images, ratings, and action buttons.
     *
     * @param model the Thymeleaf model
     * @return the {@code admin-fragrances} template name
     */
    @GetMapping("/fragrances")
    public String fragrancesPage(Model model) {
        model.addAttribute("fragrances", fragranceService.getAllFragrances());
        return "admin-fragrances";
    }

    /**
     * Renders the blank Add Fragrance form.
     *
     * <p>An empty {@link Fragrance} object is passed to the shared form template so
     * that Thymeleaf's {@code th:value} expressions produce empty inputs rather than
     * throwing null-pointer errors.
     *
     * @param model the Thymeleaf model
     * @return the {@code admin-fragrance-form} template name
     */
    @GetMapping("/fragrances/new")
    public String newFragranceForm(Model model) {
        model.addAttribute("fragrance", new Fragrance());
        model.addAttribute("actionUrl", "/admin/fragrances/new");
        model.addAttribute("pageTitle", "Add Fragrance");
        return "admin-fragrance-form";
    }

    /**
     * Processes the Add Fragrance form submission and persists the new fragrance.
     *
     * <p>The fragrance ID is left null so the H2 IDENTITY column auto-assigns it.
     * All fields except {@code description} and {@code imageUrl} are required by the
     * HTML form validation.
     *
     * @param brand       the manufacturer or fashion house name
     * @param name        the commercial fragrance name
     * @param scentFamily the olfactive family classification
     * @param projection  projection rating 1–5
     * @param longevity   longevity rating 1–5
     * @param seasonHot   hot-weather suitability 0–10
     * @param seasonCold  cold-weather suitability 0–10
     * @param officeSafe  {@code true} if appropriate for an office environment
     * @param description optional short description
     * @param imageUrl    optional URL to a bottle image
     * @param ra          flash attributes for the confirmation toast
     * @return a redirect to {@code /admin/fragrances}
     */
    @PostMapping("/fragrances/new")
    public String createFragrance(@RequestParam String  brand,
                                  @RequestParam String  name,
                                  @RequestParam String  scentFamily,
                                  @RequestParam int     projection,
                                  @RequestParam int     longevity,
                                  @RequestParam int     seasonHot,
                                  @RequestParam int     seasonCold,
                                  @RequestParam(defaultValue = "false") boolean officeSafe,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) String imageUrl,
                                  RedirectAttributes ra) {
        Fragrance f = new Fragrance();
        f.brand = brand; f.name = name; f.scentFamily = scentFamily;
        f.projection = projection; f.longevity = longevity;
        f.seasonHot = seasonHot; f.seasonCold = seasonCold;
        f.officeSafe = officeSafe; f.description = description; f.imageUrl = imageUrl;
        fragranceService.saveFragrance(f);
        fragranceService.syncToJson();
        ra.addFlashAttribute("toast", "Fragrance '" + f.name + "' added.");
        return "redirect:/admin/fragrances";
    }

    /**
     * Renders the Edit Fragrance form pre-populated with the existing fragrance's data.
     *
     * @param id    the database ID of the fragrance to edit
     * @param model the Thymeleaf model
     * @param ra    flash attributes used if the fragrance is not found
     * @return the {@code admin-fragrance-form} template, or a redirect on error
     */
    @GetMapping("/fragrances/{id}/edit")
    public String editFragranceForm(@PathVariable int id, Model model, RedirectAttributes ra) {
        Optional<Fragrance> opt = fragranceService.findFragranceById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("errorToast", "Fragrance not found."); return "redirect:/admin/fragrances"; }
        model.addAttribute("fragrance", opt.get());
        model.addAttribute("actionUrl", "/admin/fragrances/" + id + "/edit");
        model.addAttribute("pageTitle", "Edit Fragrance");
        return "admin-fragrance-form";
    }

    /**
     * Processes the Edit Fragrance form submission and saves the updated values.
     *
     * <p>The fragrance is loaded first to ensure it exists, then all fields are
     * overwritten with the submitted values and the entity is saved. The ID is
     * taken from the URL path to prevent form tampering.
     *
     * @param id          the database ID of the fragrance being edited (from the URL)
     * @param brand       updated brand name
     * @param name        updated fragrance name
     * @param scentFamily updated scent family
     * @param projection  updated projection rating 1–5
     * @param longevity   updated longevity rating 1–5
     * @param seasonHot   updated hot-weather suitability 0–10
     * @param seasonCold  updated cold-weather suitability 0–10
     * @param officeSafe  updated office-safe flag
     * @param description updated description (optional)
     * @param imageUrl    updated image URL (optional)
     * @param ra          flash attributes for the confirmation toast
     * @return a redirect to {@code /admin/fragrances}
     */
    @PostMapping("/fragrances/{id}/edit")
    public String updateFragrance(@PathVariable int id,
                                  @RequestParam String  brand,
                                  @RequestParam String  name,
                                  @RequestParam String  scentFamily,
                                  @RequestParam int     projection,
                                  @RequestParam int     longevity,
                                  @RequestParam int     seasonHot,
                                  @RequestParam int     seasonCold,
                                  @RequestParam(defaultValue = "false") boolean officeSafe,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) String imageUrl,
                                  RedirectAttributes ra) {
        Optional<Fragrance> opt = fragranceService.findFragranceById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("errorToast", "Fragrance not found."); return "redirect:/admin/fragrances"; }
        Fragrance f = opt.get();
        f.brand = brand; f.name = name; f.scentFamily = scentFamily;
        f.projection = projection; f.longevity = longevity;
        f.seasonHot = seasonHot; f.seasonCold = seasonCold;
        f.officeSafe = officeSafe; f.description = description; f.imageUrl = imageUrl;
        fragranceService.saveFragrance(f);
        fragranceService.syncToJson();
        ra.addFlashAttribute("toast", "Fragrance '" + f.name + "' updated.");
        return "redirect:/admin/fragrances";
    }

    /**
     * Deletes a fragrance from the catalogue.
     *
     * <p>Deletion is handled by {@link FragranceService#deleteFragrance(int)}, which also
     * removes the fragrance ID from every user's collection in the same transaction,
     * preventing orphaned references.
     *
     * @param id the database ID of the fragrance to delete
     * @param ra flash attributes for the confirmation toast
     * @return a redirect to {@code /admin/fragrances}
     */
    @PostMapping("/fragrances/{id}/delete")
    public String deleteFragrance(@PathVariable int id, RedirectAttributes ra) {
        fragranceService.findFragranceById(id).ifPresent(f -> {
            fragranceService.deleteFragrance(id);
            fragranceService.syncToJson();
            ra.addFlashAttribute("toast", "Fragrance '" + f.name + "' deleted.");
        });
        return "redirect:/admin/fragrances";
    }
}