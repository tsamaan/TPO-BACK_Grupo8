package com.tpo_api.haversack.dto;

import com.tpo_api.haversack.model.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Long id;
    private String sku;
    private String color;
    private String size;
    private Integer stock;
    private Double priceModifier;
    private Double finalPrice;
    private String imageUrl;
    private Boolean available;
    
    public static ProductVariantDTO fromVariant(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setStock(variant.getStock());
        dto.setPriceModifier(variant.getPriceModifier());
        dto.setFinalPrice(variant.getFinalPrice());
        dto.setImageUrl(variant.getImageUrl());
        dto.setAvailable(variant.getAvailable());
        return dto;
    }
}
