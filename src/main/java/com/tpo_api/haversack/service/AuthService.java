package com.tpo_api.haversack.service;

import com.tpo_api.haversack.config.JwtUtil;
import com.tpo_api.haversack.dto.AuthResponseDTO;
import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.dto.LoginDTO;
import com.tpo_api.haversack.exception.InvalidCredentialsException;
import com.tpo_api.haversack.exception.UnauthorizedException;
import com.tpo_api.haversack.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * @param registrationDTO Datos de registro del usuario
     * @return Usuario registrado
     * @throws BadRequestException si las contraseñas no coinciden
     * @throws ConflictException si el email o usuario ya existe
     */
    public User register(UserRegistrationDTO registrationDTO) {
        return userService.registerUser(registrationDTO);
    }
    
    /**
     * Valida las credenciales de login del usuario.
     * @param email Email del usuario
     * @param password Contraseña sin encriptar
     * @return true si las credenciales son válidas, false en caso contrario
     */
    private boolean validateLogin(String email, String password) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
    
    /**
     * Autentica un usuario y genera un token JWT.
     * @param loginDTO Credenciales de login
     * @return DTO con token y datos del usuario
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    public AuthResponseDTO login(LoginDTO loginDTO) {
        boolean isValid = validateLogin(loginDTO.getEmail(), loginDTO.getPassword());
        
        if (!isValid) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        User user = userService.getUserByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        
        // Generar token JWT
        String token = jwtUtil.generateToken(
            user.getEmail(), 
            user.getId(), 
            user.getRole().name()
        );
        
        return new AuthResponseDTO(true, "Login successful", token, user);
    }
    
    /**
     * Valida un token JWT y devuelve el usuario asociado.
     * @param authHeader Header de autorización con formato "Bearer {token}"
     * @return Usuario asociado al token
     * @throws UnauthorizedException si el token es inválido o expiró
     */
    public User validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }
        
        String email = jwtUtil.extractUsername(token);
        return userService.getUserByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
