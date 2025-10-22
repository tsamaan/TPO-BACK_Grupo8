package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.UserRegistrationDTO;
import com.tpo_api.haversack.model.Direccion;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
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
            throw new RuntimeException("Passwords do not match");
        }
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validar que el usuario no exista (si se proporciona)
        if (registrationDTO.getUsuario() != null && userRepository.existsByUsuario(registrationDTO.getUsuario())) {
            throw new RuntimeException("Username already exists");
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
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
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
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }
        
        // Solo actualizar usuario si es diferente y no existe
        if (userDTO.getUsuario() != null && !userDTO.getUsuario().equals(user.getUsuario())) {
            if (userRepository.existsByUsuario(userDTO.getUsuario())) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsuario(userDTO.getUsuario());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    public boolean validateLogin(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}
