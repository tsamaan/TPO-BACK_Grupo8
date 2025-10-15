package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.CartItemDTO;
import com.tpo_api.haversack.model.CartItem;
import com.tpo_api.haversack.model.Product;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.repository.CartItemRepository;
import com.tpo_api.haversack.repository.ProductRepository;
import com.tpo_api.haversack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }
    
    @Transactional
    public CartItem addToCart(Long userId, CartItemDTO cartItemDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + cartItemDTO.getProductId()));
        
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setCantidad(cartItemDTO.getCantidad());
        cartItem.setPrecio(cartItemDTO.getPrecio());
        
        return cartItemRepository.save(cartItem);
    }
    
    @Transactional
    public void removeFromCart(Long userId, String productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
    
    public Double calculateCartTotal(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
                .mapToDouble(item -> item.getPrecio() * item.getCantidad())
                .sum();
    }
}
