package com.tpo_api.haversack.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpo_api.haversack.exception.ApiError;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Rutas completamente públicas (sin autenticación)
                .requestMatchers("/api").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/validate-token").permitAll()

                // Rutas de consulta pública para productos y categorías (solo GET)
                .requestMatchers("GET", "/api/products/**").permitAll()
                .requestMatchers("GET", "/api/categories/**").permitAll()

                // Rutas administrativas para productos y categorías (ADMIN o SUPERADMIN)
                .requestMatchers("POST", "/api/products/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("PUT", "/api/products/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("DELETE", "/api/products/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("POST", "/api/categories/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("PUT", "/api/categories/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("DELETE", "/api/categories/**").hasAnyRole("ADMIN", "SUPERADMIN")


                // Rutas de consulta de usuarios (ADMIN o SUPERADMIN))
                .requestMatchers("GET", "/api/users").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("GET", "/api/users/{id}").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("GET", "/api/users/email/{email}").hasAnyRole("ADMIN", "SUPERADMIN")
                // Rutas de gestión de usuarios (solo SUPERADMIN)
                .requestMatchers("POST", "/api/users/register-admin").hasRole("SUPERADMIN")
                .requestMatchers("PUT", "/api/users/{id}").hasRole("SUPERADMIN")
                .requestMatchers("PUT", "/api/users/{id}/role").hasRole("SUPERADMIN")
                .requestMatchers("DELETE", "/api/users/{id}").hasRole("SUPERADMIN")

                // Rutas de carrito que requieren autenticación
                .requestMatchers("/api/cart/**").authenticated()

                // Rutas de pedidos/órdenes (requieren autenticación)
                .requestMatchers("POST", "/api/orders/**").authenticated()
                .requestMatchers("GET", "/api/orders/**").authenticated()
                .requestMatchers("GET", "/api/orders/user/**").authenticated()
                .requestMatchers("PUT", "/api/orders/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("DELETE", "/api/orders/**").hasAnyRole("ADMIN", "SUPERADMIN")

                // Cualquier otra ruta requiere autenticación por defecto
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())) // Para H2 Console
            .httpBasic(basic -> basic.realmName("Haversack API")) // Configurar HTTP Basic Auth
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .accessDeniedHandler(restAccessDeniedHandler())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // configuración CORS
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ApiError error = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", authException.getMessage(), request.getRequestURI());
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden", accessDeniedException.getMessage(), request.getRequestURI());
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
 
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
