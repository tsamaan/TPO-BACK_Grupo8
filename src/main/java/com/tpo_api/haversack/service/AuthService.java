package com.tpo_api.haversack.service;

import com.tpo_api.haversack.config.JwtUtil;
import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.dto.LoginDTO;
import com.tpo_api.haversack.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public ResponseEntity<Map<String, Object>> register(UserRegistrationDTO registrationDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.registerUser(registrationDTO);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    public ResponseEntity<Map<String, Object>> login(LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = userService.validateLogin(loginDTO.getEmail(), loginDTO.getPassword());
            if (isValid) {
                User user = userService.getUserByEmail(loginDTO.getEmail()).orElse(null);
                
                // Generar token JWT
                String token = jwtUtil.generateToken(
                    user.getEmail(), 
                    user.getId(), 
                    user.getRole().name()
                );
                
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    public ResponseEntity<Map<String, Object>> validateToken(String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractUsername(token);
                
                if (jwtUtil.validateToken(token)) {
                    User user = userService.getUserByEmail(email).orElse(null);
                    response.put("valid", true);
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                }
            }
            response.put("valid", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
