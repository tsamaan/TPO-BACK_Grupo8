package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.CartItem;
import com.tpo_api.haversack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUser(User user);
    
    List<CartItem> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    void deleteByUserIdAndProductId(Long userId, String productId);
}
