package org.spring.metro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Public read-only endpoints
                        .requestMatchers("GET", "/api/stations/**").permitAll()
                        .requestMatchers("GET", "/api/routes/**").permitAll()
                        .requestMatchers("GET", "/api/schedules/**").permitAll()

                        // Admin-only write endpoints
                        .requestMatchers("POST", "/api/stations/**", "/api/routes/**",
                                "/api/schedules/**", "/api/trains/**", "/api/fares/**")
                        .hasAuthority("StaffAdmin")
                        .requestMatchers("PUT", "/api/stations/**", "/api/routes/**",
                                "/api/schedules/**", "/api/trains/**", "/api/fares/**")
                        .hasAuthority("StaffAdmin")
                        .requestMatchers("DELETE", "/api/stations/**", "/api/routes/**",
                                "/api/schedules/**", "/api/trains/**", "/api/fares/**")
                        .hasAuthority("StaffAdmin")

                        // Maintenance issues - technical staff & admin
                        .requestMatchers("/api/maintenance-issues/**")
                        .hasAnyAuthority("TechnicalStaff", "StaffAdmin")

                        // Passenger-related endpoints - any authenticated user
                        .requestMatchers("/api/tickets/**", "/api/metrocards/**", "/api/payments/**")
                        .hasAnyAuthority("Passenger", "StaffAdmin")

                        // Everything else needs authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}