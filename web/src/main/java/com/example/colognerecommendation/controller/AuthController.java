package com.example.colognerecommendation.controller;

import com.example.colognerecommendation.model.AppUser;
import com.example.colognerecommendation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles user-facing authentication pages: login and registration.
 *
 * <p>Spring Security intercepts and processes the actual login POST automatically —
 * this controller only needs to serve the login page GET and communicate status messages
 * (error, logout) via model attributes that the Thymeleaf template reads.
 *
 * <p>Registration is fully handled here: validation, uniqueness check, password hashing,
 * and account persistence.
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Serves the login page.
     *
     * <p>Spring Security appends {@code ?logout} to the URL after a successful logout,
     * and {@code ?error} after a failed login attempt. This method translates those
     * query parameters into user-friendly messages added to the model.
     *
     * @param logout non-null if the user just signed out (Spring Security sets this param)
     * @param error  non-null if the previous login attempt failed
     * @param model  the Thymeleaf model to add status messages to
     * @return the {@code login} template name
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String logout,
                            @RequestParam(required = false) String error,
                            Model model) {
        if (logout != null) model.addAttribute("logoutMsg", "You've been signed out.");
        if (error  != null) model.addAttribute("errorMsg",  "Invalid username or password.");
        return "login";
    }

    /**
     * Serves the registration form.
     *
     * @return the {@code register} template name
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Processes the registration form submission.
     *
     * <p>Validation is performed in the following order — the first failure short-circuits
     * and redirects back with an error message:
     * <ol>
     *   <li>Username must not be blank and password must be at least 6 characters.</li>
     *   <li>Password and confirmation must match.</li>
     *   <li>Username must not already be taken.</li>
     * </ol>
     *
     * <p>If all checks pass, a new {@link AppUser} is created. The password is encoded
     * with BCrypt before being stored — the plain-text value is never persisted.
     *
     * <p>The first account ever created is automatically assigned the {@code "ADMIN"} role;
     * all subsequent accounts receive {@code "USER"}.
     *
     * @param username        the chosen login name
     * @param password        the chosen password (plain text, only used transiently)
     * @param confirmPassword must match {@code password}
     * @param ra              flash attributes used to pass success or error messages across the redirect
     * @return a redirect to {@code /register} on failure, or to {@code /login} on success
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes ra) {
        if (username.isBlank() || password.length() < 6) {
            ra.addFlashAttribute("errorMsg", "Username required; password must be at least 6 characters.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMsg", "Passwords do not match.");
            return "redirect:/register";
        }
        if (userRepository.existsByUsername(username)) {
            ra.addFlashAttribute("errorMsg", "Username already taken.");
            return "redirect:/register";
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(userRepository.count() == 0 ? "ADMIN" : "USER");
        userRepository.save(user);
        ra.addFlashAttribute("successMsg", "Account created! Please sign in.");
        return "redirect:/login";
    }
}
