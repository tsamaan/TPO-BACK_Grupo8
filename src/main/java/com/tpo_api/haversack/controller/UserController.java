package com.tpo_api.haversack.controller;

import com.tpo_api.haversack.dto.LoginDTO;
import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
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
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = userService.validateLogin(loginDTO.getEmail(), loginDTO.getPassword());
            if (isValid) {
                User user = userService.getUserByEmail(loginDTO.getEmail()).orElse(null);
                response.put("success", true);
                response.put("message", "Login successful");
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
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserRegistrationDTO userDTO) {
        try {
            User updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
