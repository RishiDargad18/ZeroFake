package com.zerofake.blockchain.config;

import com.zerofake.blockchain.security.JwtAuthenticationFilter;
import com.zerofake.blockchain.security.handler.RestAccessDeniedHandler;
import com.zerofake.blockchain.security.handler.RestAuthenticationEntryPoint;
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
 * Access rules for ledger operations.
 *
 * <p>Writes to the ledger are irreversible, so they are restricted to the roles
 * that legitimately hold custody of a product. Reads — verification, history and
 * the transaction audit log — are open to any authenticated user.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String MANUFACTURER = "ROLE_MANUFACTURER";
    private static final String WAREHOUSE = "ROLE_WAREHOUSE";
    private static final String DISTRIBUTOR = "ROLE_DISTRIBUTOR";
    private static final String RETAILER = "ROLE_RETAILER";

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

                        // Only a manufacturer may mint a product's on-chain identity.
                        .requestMatchers(HttpMethod.POST, "/api/v1/blockchain/register-product")
                        .hasAnyAuthority(ADMIN, MANUFACTURER)

                        // Custody may only be handed on by a supply chain participant.
                        .requestMatchers(HttpMethod.POST, "/api/v1/blockchain/transfer-ownership")
                        .hasAnyAuthority(ADMIN, MANUFACTURER, WAREHOUSE, DISTRIBUTOR, RETAILER)

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
