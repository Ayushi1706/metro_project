package org.spring.metro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // =========================
    // Password Encoder
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // Authentication Manager
    // =========================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    // =========================
    // Security Filter Chain
    // =========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // Disable CSRF because we are using JWT
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // JWT = Stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =========================
                // Authorization Rules
                // =========================
                .authorizeHttpRequests(auth -> auth

                        // ---------------------------------
                        // PUBLIC AUTH ENDPOINTS
                        // ---------------------------------
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()


                        // ---------------------------------
                        // PUBLIC GET ENDPOINTS
                        // ---------------------------------

                        // Stations
                        .requestMatchers(HttpMethod.GET, "/api/stations/**")
                        .permitAll()

                        // Routes
                        .requestMatchers(HttpMethod.GET, "/api/routes/**")
                        .permitAll()

                        // Schedules
                        .requestMatchers(HttpMethod.GET, "/api/schedules/**")
                        .permitAll()

                        // Trains
                        .requestMatchers(HttpMethod.GET, "/api/trains/**")
                        .permitAll()


                        // ---------------------------------
                        // ADMIN ONLY
                        // ---------------------------------

                        // Create
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/stations/**",
                                "/api/routes/**",
                                "/api/schedules/**",
                                "/api/trains/**",
                                "/api/fares/**"
                        )
                        .hasRole("ADMIN")

                        // Update
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/stations/**",
                                "/api/routes/**",
                                "/api/schedules/**",
                                "/api/trains/**",
                                "/api/fares/**"
                        )
                        .hasRole("ADMIN")

                        // Delete
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/stations/**",
                                "/api/routes/**",
                                "/api/schedules/**",
                                "/api/trains/**",
                                "/api/fares/**"
                        )
                        .hasRole("ADMIN")


                        // ---------------------------------
                        // MAINTENANCE
                        // ---------------------------------
                        .requestMatchers("/api/maintenance-issues/**")
                        .hasAnyRole(
                                "TECHNICAL_STAFF",
                                "ADMIN"
                        )


                        // ---------------------------------
                        // PASSENGER / ADMIN
                        // ---------------------------------
                        .requestMatchers(
                                "/api/tickets/**",
                                "/api/metrocards/**",
                                "/api/payments/**"
                        )
                        .hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )


                        // ---------------------------------
                        // EVERYTHING ELSE
                        // ---------------------------------
                        .anyRequest()
                        .authenticated()
                )

                // JWT filter runs before Spring's
                // UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    // =========================
    // CORS Configuration
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}