package com.example.colognerecommendation.security;

import com.example.colognerecommendation.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads an {@link com.example.colognerecommendation.model.AppUser} from the database
 * and wraps it in a Spring Security {@link UserDetails} object.
 *
 * <p>Extracted into its own {@code @Service} so that both {@link JwtAuthFilter} and
 * {@link com.example.colognerecommendation.config.SecurityConfig} can inject it
 * without creating a circular dependency.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
