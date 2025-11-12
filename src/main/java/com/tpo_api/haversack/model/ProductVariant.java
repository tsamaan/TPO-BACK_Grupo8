package com.tpo_api.haversack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;
    
    @Column(nullable = false, unique = true)
    private String sku; // Stock Keeping Unit - código único de la variante
    
    @Column(nullable = false)
    private String color;
    
    private String size; // Opcional: talla (S, M, L, XL, etc.)
    
    @Column(nullable = false)
    private Integer stock;
    
    private Double priceModifier; // Modificador de precio (puede ser null si usa el precio base)
    
    @Column(name = "image_url")
    private String imageUrl; // Imagen específica de esta variante
    
    private Boolean available = true; // Si la variante está disponible para venta
    
    /**
     * Calcula el precio final de esta variante
     * @return precio base del producto + modificador (si existe)
     */
    public Double getFinalPrice() {
        if (product == null) return 0.0;
        Double basePrice = product.getPrice();
        if (priceModifier != null) {
            return basePrice + priceModifier;
        }
        return basePrice;
    }
    
    /**
     * Verifica si hay stock disponible
     */
    public boolean hasStock() {
        return stock != null && stock > 0 && available;
    }
    
    /**
     * Verifica si hay stock suficiente para una cantidad dada
     */
    public boolean hasStock(int quantity) {
        return stock != null && stock >= quantity && available;
    }
}
