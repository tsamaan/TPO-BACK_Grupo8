package com.tpo_api.haversack.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String id;
    private Long variantId;  // ID de la variante para reducir stock
    private String sku;      // SKU único de la variante
    private String name;
    private Integer cantidad;
    private Double precio;
    private String color;    // Color de la variante
    private String size;     // Tamaño de la variante
}
