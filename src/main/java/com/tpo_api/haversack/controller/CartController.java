package com.tpo_api.haversack.controller;

import com.tpo_api.haversack.dto.CartItemDTO;
import com.tpo_api.haversack.model.CartItem;
import com.tpo_api.haversack.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        return ResponseEntity.ok(cartItems);
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<CartItem> addToCart(@PathVariable Long userId, @RequestBody CartItemDTO cartItemDTO) {
        CartItem cartItem = cartService.addToCart(userId, cartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
    }
    
    @DeleteMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long userId, @PathVariable String productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Map<String, Double>> getCartTotal(@PathVariable Long userId) {
        Double total = cartService.calculateCartTotal(userId);
        Map<String, Double> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
}