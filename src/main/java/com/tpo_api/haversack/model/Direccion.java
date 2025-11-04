package com.tpo_api.haversack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    
    @Column(name = "calle")
    private String calle;
    
    @Column(name = "ciudad")
    private String ciudad;
    
    @Column(name = "codigo_postal")
    private String cp;
    
    @Column(name = "pais")
    private String pais;
}
