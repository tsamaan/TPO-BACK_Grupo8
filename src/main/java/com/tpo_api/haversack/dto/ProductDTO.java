package com.tpo_api.haversack.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private List<String> images;
    private String stock;
    
    // Soporte para ambas formas de especificar categoría
    private Long categoryId;        // Por ID (recomendado)
    private String categoryName;    // Por nombre (para compatibilidad)
    
    private Integer quantity;
    private List<String> colores;
    private List<String> tags;
}
