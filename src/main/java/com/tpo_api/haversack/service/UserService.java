package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.exception.BadRequestException;
import com.tpo_api.haversack.exception.ConflictException;
import com.tpo_api.haversack.exception.NotFoundException;
import com.tpo_api.haversack.model.Direccion;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserByUsuario(String usuario) {
        return userRepository.findByUsuario(usuario);
    }
    
    public User registerUser(UserRegistrationDTO registrationDTO) {
        // Validar que las contraseñas coincidan
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        
        // Validar que el usuario no exista (si se proporciona)
        if (registrationDTO.getUsuario() != null && userRepository.existsByUsuario(registrationDTO.getUsuario())) {
            throw new ConflictException("Username already exists");
        }
        
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setNombre(registrationDTO.getNombre());
        user.setApellido(registrationDTO.getApellido());
        user.setUsuario(registrationDTO.getUsuario());
        user.setName(registrationDTO.getName());
        user.setPhone(registrationDTO.getPhone());
        
        // Configurar dirección embebida si se proporciona
        if (registrationDTO.getAddress() != null) {
            Direccion direccion = new Direccion();
            direccion.setCalle(registrationDTO.getAddress());
            user.setDireccion(direccion);
        }
        user.setRole(User.Role.USER);
        
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, UserRegistrationDTO userDTO) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        
        if (userDTO.getNombre() != null) user.setNombre(userDTO.getNombre());
        if (userDTO.getApellido() != null) user.setApellido(userDTO.getApellido());
        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        
        // Actualizar dirección embebida si se proporciona
        if (userDTO.getAddress() != null) {
            Direccion direccion = new Direccion();
            direccion.setCalle(userDTO.getAddress());
            user.setDireccion(direccion);
        }
        
        // Solo actualizar email si es diferente y no existe
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }
        
        // Solo actualizar usuario si es diferente y no existe
        if (userDTO.getUsuario() != null && !userDTO.getUsuario().equals(user.getUsuario())) {
            if (userRepository.existsByUsuario(userDTO.getUsuario())) {
                throw new ConflictException("Username already exists");
            }
            user.setUsuario(userDTO.getUsuario());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User changeUserRole(Long id, User.Role newRole) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setRole(newRole);
        return userRepository.save(user);
    }
    
    /**
     * Cambia el rol de un usuario validando el string del rol.
     * @param id ID del usuario
     * @param roleString String con el nombre del rol (case-insensitive)
     * @return Usuario actualizado
     * @throws NotFoundException si el usuario no existe
     * @throws BadRequestException si el rol es inválido o nulo
     */
    public User changeUserRole(Long id, String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new BadRequestException("Role is required");
        }
        
        User.Role newRole;
        try {
            newRole = User.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be USER, ADMIN, or SUPERADMIN");
        }
        
        return changeUserRole(id, newRole);
    }

    /**
     * Obtiene el usuario actualmente autenticado desde el contexto de seguridad.
     * @return Usuario autenticado
     * @throws UnauthorizedException si no hay autenticación o es inválida
     */
    public User getCurrentAuthenticatedUser() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.tpo_api.haversack.exception.UnauthorizedException("Not authenticated");
        }
        
        String email = authentication.getName();
        return getUserByEmail(email)
                .orElseThrow(() -> new com.tpo_api.haversack.exception.NotFoundException("User not found"));
    }
    
    /**
     * Registra un usuario con un rol específico.
     * @param registrationDTO Datos de registro
     * @param role Rol a asignar
     * @return Usuario registrado
     * @throws BadRequestException si las contraseñas no coinciden
     * @throws ConflictException si el email o usuario ya existe
     */
    public User registerUserWithRole(UserRegistrationDTO registrationDTO, User.Role role) {
        // Validar que las contraseñas coincidan
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        
        // Validar que el usuario no exista (si se proporciona)
        if (registrationDTO.getUsuario() != null && userRepository.existsByUsuario(registrationDTO.getUsuario())) {
            throw new ConflictException("Username already exists");
        }
        
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setNombre(registrationDTO.getNombre());
        user.setApellido(registrationDTO.getApellido());
        user.setUsuario(registrationDTO.getUsuario());
        user.setName(registrationDTO.getName());
        user.setPhone(registrationDTO.getPhone());
        
        // Configurar dirección embebida si se proporciona
        if (registrationDTO.getAddress() != null) {
            Direccion direccion = new Direccion();
            direccion.setCalle(registrationDTO.getAddress());
            user.setDireccion(direccion);
        }
        user.setRole(role);
        
        return userRepository.save(user);
    }
}
