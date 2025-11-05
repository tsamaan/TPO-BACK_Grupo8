package com.tpo_api.haversack.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpo_api.haversack.exception.ApiError;
import org.springframework.http.HttpStatus;

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
                
                // Rutas de consulta pública para productos y categorías (solo GET)
                .requestMatchers("GET", "/api/products/**").permitAll()
                .requestMatchers("GET", "/api/categories/**").permitAll()
                
                // Rutas administrativas que requieren autenticación
                .requestMatchers("POST", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("POST", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/categories/**").hasRole("ADMIN")

                // Rutas de usuario que requieren autenticación
                .requestMatchers("/api/users").hasRole("ADMIN")
                .requestMatchers("/api/users/{id}").hasRole("ADMIN")
                .requestMatchers("/api/users/email/{email}").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/users/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/users/**").hasRole("ADMIN")

                // Rutas de carrito que requieren autenticación
                .requestMatchers("/api/cart/**").authenticated()
                
                // Rutas de órdenes
                .requestMatchers("/api/orders/guest").permitAll() // Permitir órdenes de invitados
                .requestMatchers("/api/orders/**").authenticated() // Resto requiere autenticación
                
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
 
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
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
