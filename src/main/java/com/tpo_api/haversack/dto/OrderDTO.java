package com.tpo_api.haversack.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private List<OrderItemDTO> productos;
    private Double total;
}
