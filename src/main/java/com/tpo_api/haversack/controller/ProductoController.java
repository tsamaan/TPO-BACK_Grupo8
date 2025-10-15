package com.tpo_api.haversack.controller;

import com.tpo_api.haversack.dto.ProductoDTO;
import com.tpo_api.haversack.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> getAllProductos() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable String id) {
        Optional<ProductoDTO> producto = productoService.findById(id);
        return producto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductoDTO createProducto(@RequestBody ProductoDTO productoDTO) {
        return productoService.save(productoDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> updateProducto(@PathVariable String id, @RequestBody ProductoDTO productoDTO) {
        productoDTO.setId(id);
        ProductoDTO updatedProducto = productoService.save(productoDTO);
        return ResponseEntity.ok(updatedProducto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
