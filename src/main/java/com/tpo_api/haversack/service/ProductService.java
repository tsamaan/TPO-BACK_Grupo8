package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.ProductDTO;
import com.tpo_api.haversack.model.Product;
import com.tpo_api.haversack.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Product> getProductsByTags(List<String> tags) {
        return productRepository.findByTagsIn(tags);
    }
    
    public List<Product> getProductsByCategoryAndPriceRange(String category, Double minPrice, Double maxPrice) {
        return productRepository.findByCategoryAndPriceBetween(category, minPrice, maxPrice);
    }
    
    public Product createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        return productRepository.save(product);
    }
    
    public Product updateProduct(String id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        updateProductFromDTO(product, productDTO);
        return productRepository.save(product);
    }
    
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
    
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImage(dto.getImage());
        product.setImages(dto.getImages());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setQuantity(dto.getQuantity());
        product.setColores(dto.getColores());
        product.setTags(dto.getTags());
        return product;
    }
    
    private void updateProductFromDTO(Product product, ProductDTO dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getImages() != null) product.setImages(dto.getImages());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getQuantity() != null) product.setQuantity(dto.getQuantity());
        if (dto.getColores() != null) product.setColores(dto.getColores());
        if (dto.getTags() != null) product.setTags(dto.getTags());
    }
}
