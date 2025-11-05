package com.tpo_api.haversack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping
    public Map<String, Object> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "Haversack E-commerce API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("description", "API REST para el e-commerce de mochilas Haversack");
        
        Map<String, Object> endpoints = new HashMap<>();
        
        // Products endpoints
        Map<String, String> products = new HashMap<>();
        products.put("GET /api/products", "Obtener todos los productos");
        products.put("GET /api/products/{id}", "Obtener producto por ID");
        products.put("GET /api/products/category/{category}", "Obtener productos por categoría");
        products.put("GET /api/products/search?name={name}", "Buscar productos por nombre");
        products.put("GET /api/products/price-range?minPrice={min}&maxPrice={max}", "Filtrar por rango de precio");
        products.put("GET /api/products/tags?tags={tag1,tag2}", "Filtrar por tags");
        products.put("POST /api/products", "Crear nuevo producto");
        products.put("PUT /api/products/{id}", "Actualizar producto");
        products.put("DELETE /api/products/{id}", "Eliminar producto");
        
        // Users endpoints
        Map<String, String> users = new HashMap<>();
        users.put("GET /api/users", "Obtener todos los usuarios");
        users.put("GET /api/users/{id}", "Obtener usuario por ID");
        users.put("GET /api/users/email/{email}", "Obtener usuario por email");
        users.put("POST /api/users/register", "Registrar nuevo usuario");
        users.put("POST /api/users/login", "Iniciar sesión");
        users.put("PUT /api/users/{id}", "Actualizar usuario");
        users.put("DELETE /api/users/{id}", "Eliminar usuario");
        
        // Cart endpoints
        Map<String, String> cart = new HashMap<>();
        cart.put("GET /api/cart/user/{userId}", "Obtener carrito del usuario");
        cart.put("POST /api/cart/user/{userId}", "Agregar producto al carrito");
        cart.put("DELETE /api/cart/user/{userId}/product/{productId}", "Eliminar producto del carrito");
        cart.put("DELETE /api/cart/user/{userId}", "Vaciar carrito");
        cart.put("GET /api/cart/user/{userId}/total", "Obtener total del carrito");
        
        // Orders endpoints
        Map<String, String> orders = new HashMap<>();
        orders.put("GET /api/orders", "Obtener todas las órdenes");
        orders.put("GET /api/orders/{id}", "Obtener orden por ID");
        orders.put("GET /api/orders/email/{email}", "Obtener órdenes por email");
        orders.put("GET /api/orders/status/{status}", "Obtener órdenes por estado");
        orders.put("POST /api/orders", "Crear nueva orden");
        orders.put("PUT /api/orders/{id}/status?status={status}", "Actualizar estado de orden");
        orders.put("DELETE /api/orders/{id}", "Eliminar orden");
        
        endpoints.put("products", products);
        endpoints.put("users", users);
        endpoints.put("cart", cart);
        endpoints.put("orders", orders);
        
        apiInfo.put("endpoints", endpoints);
        
        Map<String, String> database = new HashMap<>();
        database.put("h2-console", "http://localhost:8080/h2-console");
        database.put("jdbc-url", "jdbc:h2:mem:testdb");
        database.put("username", "sa");
        database.put("password", "");
        
        apiInfo.put("database", database);
        
        return apiInfo;
    }
}
