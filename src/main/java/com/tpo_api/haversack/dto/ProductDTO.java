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
    private String category;
    private Integer quantity;
    private List<String> colores;
    private List<String> tags;
}
