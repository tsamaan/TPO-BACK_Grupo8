package com.tpo_api.haversack.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private String productId;
    private Integer cantidad;
    private Double precio;
}
