package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.ProductDTO;
import com.tpo_api.haversack.dto.ProductVariantDTO;
import com.tpo_api.haversack.model.Product;
import com.tpo_api.haversack.model.Category;
import com.tpo_api.haversack.model.ProductVariant;
import com.tpo_api.haversack.repository.ProductRepository;
import com.tpo_api.haversack.repository.CategoryRepository;
import com.tpo_api.haversack.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
        // .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    public List<Product> getProductsByCategoryName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
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
    
    public List<Product> getProductsByCategoryAndPriceRange(Long categoryId, Double minPrice, Double maxPrice) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return productRepository.findByCategoryAndPriceBetween(category, minPrice, maxPrice);
    }
    
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        
        // Crear variantes si se proporcionan
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO variantDTO : productDTO.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSku(variantDTO.getSku());
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStock(variantDTO.getStock());
                variant.setPriceModifier(variantDTO.getPriceModifier());
                variant.setImageUrl(variantDTO.getImageUrl());
                variant.setAvailable(variantDTO.getAvailable() != null ? variantDTO.getAvailable() : true);
                
                productVariantRepository.save(variant);
            }
        }
        
        return savedProduct;
    }
    
    @Transactional
    public Product updateProduct(String id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        updateProductFromDTO(product, productDTO);
        
        // Actualizar variantes si se proporcionan
        if (productDTO.getVariants() != null) {
            updateProductVariants(product, productDTO.getVariants());
        }
        
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
        
        // Buscar la categoría por ID o nombre
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        } else if (dto.getCategoryName() != null) {
            Category category = categoryRepository.findByName(dto.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found with name: " + dto.getCategoryName()));
            product.setCategory(category);
        }
        
        product.setTags(dto.getTags());
        
        // NOTA: Las variantes (stock, colores) se gestionan por separado
        // No se crean automáticamente aquí, deben crearse mediante ProductVariant
        
        return product;
    }
    
    private void updateProductFromDTO(Product product, ProductDTO dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getImages() != null) product.setImages(dto.getImages());
        
        // Actualizar categoría si se proporciona
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        } else if (dto.getCategoryName() != null) {
            Category category = categoryRepository.findByName(dto.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found with name: " + dto.getCategoryName()));
            product.setCategory(category);
        }
        
        if (dto.getTags() != null) product.setTags(dto.getTags());
    }
    
    /**
     * Actualiza las variantes de un producto
     * Estrategia mejorada: actualiza variantes existentes por SKU o crea nuevas
     */
    private void updateProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
        // Obtener variantes existentes
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
        
        // Crear mapa de variantes existentes por SKU
        var existingBySku = existingVariants.stream()
                .collect(java.util.stream.Collectors.toMap(
                    ProductVariant::getSku, 
                    v -> v,
                    (v1, v2) -> v1
                ));
        
        // Crear set de SKUs que vienen en el DTO
        var incomingSkus = variantDTOs.stream()
                .map(ProductVariantDTO::getSku)
                .collect(java.util.stream.Collectors.toSet());
        
        // Eliminar variantes que ya no están en el DTO
        existingVariants.stream()
                .filter(v -> !incomingSkus.contains(v.getSku()))
                .forEach(productVariantRepository::delete);
        
        // Actualizar o crear variantes
        for (ProductVariantDTO variantDTO : variantDTOs) {
            ProductVariant variant = existingBySku.get(variantDTO.getSku());
            
            if (variant != null) {
                // Actualizar variante existente
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStock(variantDTO.getStock());
                variant.setPriceModifier(variantDTO.getPriceModifier());
                variant.setImageUrl(variantDTO.getImageUrl());
                variant.setAvailable(variantDTO.getAvailable() != null ? variantDTO.getAvailable() : true);
                productVariantRepository.save(variant);
            } else {
                // Crear nueva variante
                variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSku(variantDTO.getSku());
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStock(variantDTO.getStock());
                variant.setPriceModifier(variantDTO.getPriceModifier());
                variant.setImageUrl(variantDTO.getImageUrl());
                variant.setAvailable(variantDTO.getAvailable() != null ? variantDTO.getAvailable() : true);
                productVariantRepository.save(variant);
            }
        }
    }
}

//TODO: agregar manejo de excepciones personalizadas en el service para casos como "Producto no encontrado", "Categoría no encontrada", etc.