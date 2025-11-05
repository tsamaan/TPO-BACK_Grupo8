package com.tpo_api.haversack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    private String image;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    // Relación con variantes (cada producto puede tener múltiples variantes)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ProductVariant> variants = new ArrayList<>();
    
    /**
     * Calcula el stock total sumando todas las variantes
     */
    @Transient
    public Integer getTotalStock() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.stream()
                .filter(v -> v.getAvailable())
                .mapToInt(ProductVariant::getStock)
                .sum();
    }
    
    /**
     * Obtiene todos los colores disponibles (sin duplicados)
     */
    @Transient
    public List<String> getAvailableColors() {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        return variants.stream()
                .filter(v -> v.getAvailable() && v.hasStock())
                .map(ProductVariant::getColor)
                .distinct()
                .toList();
    }
    
    /**
     * Obtiene el precio mínimo de todas las variantes
     */
    @Transient
    public Double getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return price;
        }
        return variants.stream()
                .filter(ProductVariant::getAvailable)
                .map(ProductVariant::getFinalPrice)
                .min(Double::compare)
                .orElse(price);
    }
    
    /**
     * Obtiene el precio máximo de todas las variantes
     */
    @Transient
    public Double getMaxPrice() {
        if (variants == null || variants.isEmpty()) {
            return price;
        }
        return variants.stream()
                .filter(ProductVariant::getAvailable)
                .map(ProductVariant::getFinalPrice)
                .max(Double::compare)
                .orElse(price);
    }
}
