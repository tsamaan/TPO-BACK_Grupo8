package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsuario(String usuario);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsuario(String usuario);
}
