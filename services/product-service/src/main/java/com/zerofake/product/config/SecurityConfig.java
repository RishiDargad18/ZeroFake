package com.zerofake.product.config;

import com.zerofake.product.security.JwtAuthenticationFilter;
import com.zerofake.product.security.handler.RestAccessDeniedHandler;
import com.zerofake.product.security.handler.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Access rules for the product catalogue.
 *
 * <p>Reads are available to every authenticated user, because a customer must be
 * able to look up a product they have just scanned. Writes are restricted to the
 * roles that own the catalogue: administrators and manufacturers.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String MANUFACTURER = "ROLE_MANUFACTURER";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${zerofake.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Categories are reference data: only administrators curate them.
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAuthority(ADMIN)

                        // The blockchain service calls this endpoint on the caller's
                        // behalf after a successful on-chain registration.
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/*/blockchain-status")
                        .hasAnyAuthority(ADMIN, MANUFACTURER)

                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**", "/api/v1/batches/**")
                        .hasAnyAuthority(ADMIN, MANUFACTURER)

                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**", "/api/v1/batches/**")
                        .hasAnyAuthority(ADMIN, MANUFACTURER)

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**", "/api/v1/batches/**")
                        .hasAnyAuthority(ADMIN, MANUFACTURER)

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
