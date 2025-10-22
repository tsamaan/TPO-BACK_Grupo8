package com.tpo_api.haversack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Rutas completamente públicas (sin autenticación)
                .requestMatchers("/api").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                
                // Rutas de consulta pública para productos y categorías (solo GET)
                .requestMatchers("GET", "/api/products/**").permitAll()
                .requestMatchers("GET", "/api/categories/**").permitAll()
                
                // Rutas administrativas que requieren autenticación
                .requestMatchers("POST", "/api/products/**").authenticated()
                .requestMatchers("PUT", "/api/products/**").authenticated()
                .requestMatchers("DELETE", "/api/products/**").authenticated()
                .requestMatchers("POST", "/api/categories/**").authenticated()
                .requestMatchers("PUT", "/api/categories/**").authenticated()
                .requestMatchers("DELETE", "/api/categories/**").authenticated()
                
                // Rutas de usuario que requieren autenticación
                .requestMatchers("/api/users").authenticated()
                .requestMatchers("/api/users/{id}").authenticated()
                .requestMatchers("/api/users/email/{email}").authenticated()
                .requestMatchers("PUT", "/api/users/**").authenticated()
                .requestMatchers("DELETE", "/api/users/**").authenticated()
                
                // Rutas de carrito que requieren autenticación
                .requestMatchers("/api/cart/**").authenticated()
                
                // Rutas de órdenes que requieren autenticación
                .requestMatchers("/api/orders/**").authenticated()
                
                // Cualquier otra ruta requiere autenticación por defecto
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())) // Para H2 Console
            .httpBasic(basic -> basic.realmName("Haversack API")); // Configurar HTTP Basic Auth

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
