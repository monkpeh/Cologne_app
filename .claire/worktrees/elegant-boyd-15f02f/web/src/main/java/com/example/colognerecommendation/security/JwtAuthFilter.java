package com.example.colognerecommendation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request and validates the JWT token in the {@code Authorization} header.
 *
 * <p>If a valid {@code Bearer <token>} header is present and the token belongs to a known
 * user, this filter populates the {@link org.springframework.security.core.context.SecurityContext}
 * so downstream Spring Security checks see the user as authenticated.
 *
 * <p>Requests without a token, or with an invalid/expired token, simply pass through
 * unauthenticated — the security filter chain will then reject them if the endpoint requires auth.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil               jwtUtil;
    private final AppUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, AppUserDetailsService userDetailsService) {
        this.jwtUtil            = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token    = authHeader.substring(7);
            String username = null;

            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception ignored) {
                // Malformed or expired — leave username null, filter chain will reject
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
