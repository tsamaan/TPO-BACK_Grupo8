package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.ProductoDTO;
import com.tpo_api.haversack.model.Producto;
import com.tpo_api.haversack.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<ProductoDTO> findAll() {
        return productoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductoDTO> findById(String id) {
        return productoRepository.findById(id)
                .map(this::convertToDTO);
    }

    public ProductoDTO save(ProductoDTO productoDTO) {
        Producto producto = convertToEntity(productoDTO);
        Producto savedProducto = productoRepository.save(producto);
        return convertToDTO(savedProducto);
    }

    public void deleteById(String id) {
        productoRepository.deleteById(id);
    }

    private ProductoDTO convertToDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setName(producto.getName());
        dto.setDescription(producto.getDescription());
        dto.setPrice(producto.getPrice());
        dto.setImage(producto.getImage());
        dto.setImages(producto.getImages());
        dto.setStock(producto.getStock());
        dto.setCategory(producto.getCategory());
        dto.setQuantity(producto.getQuantity());
        dto.setColores(producto.getColores());
        dto.setTags(producto.getTags());
        return dto;
    }

    private Producto convertToEntity(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setName(dto.getName());
        producto.setDescription(dto.getDescription());
        producto.setPrice(dto.getPrice());
        producto.setImage(dto.getImage());
        producto.setImages(dto.getImages());
        producto.setStock(dto.getStock());
        producto.setCategory(dto.getCategory());
        producto.setQuantity(dto.getQuantity());
        producto.setColores(dto.getColores());
        producto.setTags(dto.getTags());
        return producto;
    }
}
