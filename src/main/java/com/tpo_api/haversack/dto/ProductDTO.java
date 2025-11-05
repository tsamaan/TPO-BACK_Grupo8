package com.tpo_api.haversack.dto;

import com.tpo_api.haversack.model.Category;
import com.tpo_api.haversack.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private Double price; // Precio base
    private Double minPrice; // Precio mínimo de variantes
    private Double maxPrice; // Precio máximo de variantes
    private String image;
    private List<String> images;
    private Category category;
    private List<String> tags;
    private Integer totalStock; // Stock total de todas las variantes
    private List<String> colores; // Colores disponibles (para compatibilidad)
    private List<ProductVariantDTO> variants; // Todas las variantes
    
    // Campos para compatibilidad con frontend antiguo
    private Long categoryId;
    private String categoryName;
    private String stock; // Para compatibilidad
    private Integer quantity;
    
    /**
     * Convierte un Product a ProductDTO
     */
    public static ProductDTO fromProduct(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setMinPrice(product.getMinPrice());
        dto.setMaxPrice(product.getMaxPrice());
        dto.setImage(product.getImage());
        dto.setImages(product.getImages());
        dto.setCategory(product.getCategory());
        dto.setTags(product.getTags());
        dto.setTotalStock(product.getTotalStock());
        dto.setColores(product.getAvailableColors());
        
        // Compatibilidad
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setStock(String.valueOf(product.getTotalStock()));
        dto.setQuantity(product.getTotalStock());
        
        // Convertir variantes
        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(ProductVariantDTO::fromVariant)
                    .collect(Collectors.toList()));
        } else {
            dto.setVariants(new ArrayList<>());
        }
        
        return dto;
    }
}
