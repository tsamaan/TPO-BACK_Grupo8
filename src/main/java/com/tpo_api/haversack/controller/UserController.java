package com.tpo_api.haversack.controller;

import com.tpo_api.haversack.dto.LoginDTO;
import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.exception.BadRequestException;
import com.tpo_api.haversack.exception.NotFoundException;
import com.tpo_api.haversack.exception.UnauthorizedException;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.service.AuthService;
import com.tpo_api.haversack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        return authService.register(registrationDTO);
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }
    
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        return authService.validateToken(authHeader);
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserRegistrationDTO userDTO) {
        User updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String roleString = request.get("role");
        if (roleString == null) {
            throw new BadRequestException("Role is required");
        }

        User.Role newRole;
        if (!isValidRole(roleString)) {
            throw new BadRequestException("Invalid role. Must be USER, ADMIN, or SUPERADMIN");
        }
        newRole = User.Role.valueOf(roleString.toUpperCase());

        User updatedUser = userService.changeUserRole(id, newRole);
        response.put("success", true);
        response.put("message", "User role updated successfully");
        response.put("user", updatedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@RequestBody UserRegistrationDTO registrationDTO) {
        Map<String, Object> response = new HashMap<>();
        User admin = userService.registerUserWithRole(registrationDTO, User.Role.ADMIN);
        response.put("success", true);
        response.put("message", "Admin registered successfully");
        response.put("user", admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean isValidRole(String roleString) {
        try {
            User.Role.valueOf(roleString.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

//TODO: Revisar la separacion en capas. Controler maneja las solicitudes http. services maneja el modelo de negocio.
