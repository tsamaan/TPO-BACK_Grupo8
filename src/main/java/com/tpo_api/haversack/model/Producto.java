package com.tpo_api.haversack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Producto {
    
    @Id
    private String id;
    private String name;
    private String description;
    private Integer price;
    private String image;
    
    @ElementCollection
    private List<String> images;
    
    private String stock;
    private String category;
    private Integer quantity;
    
    @ElementCollection
    private List<String> colores;
    
    @ElementCollection
    private List<String> tags;
}