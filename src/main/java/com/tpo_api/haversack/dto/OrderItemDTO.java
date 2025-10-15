package com.tpo_api.haversack.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String id;
    private String name;
    private Integer cantidad;
    private Double precio;
}
