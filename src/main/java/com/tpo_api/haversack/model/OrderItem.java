package com.tpo_api.haversack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(nullable = false)
    private Double precio;
}
