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
    private final jakarta.persistence.EntityManager entityManager;
    
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
        // PASO 1: Cargar producto existente
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // PASO 2: Actualizar propiedades básicas del producto
        updateProductFromDTO(product, productDTO);
        product = productRepository.save(product);

        // PASO 3: Actualizar variantes (incluyendo eliminaciones)
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            updateProductVariants(product, productDTO.getVariants());
        }

        // PASO 4: Forzar persistencia de TODOS los cambios (incluyendo deletes)
        entityManager.flush();

        // PASO 5: Limpiar contexto y recargar producto fresh
        entityManager.clear();
        product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found after update"));

        // PASO 6: Inicializar lazy collections para serialización JSON
        org.hibernate.Hibernate.initialize(product.getVariants());
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> org.hibernate.Hibernate.initialize(v));
        }

        return product;
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

        // FIX: Manejar categoría de múltiples formas con mejor validación
        Category categoryToSet = null;

        // Opción 1: categoryId explícito (preferido)
        if (dto.getCategoryId() != null) {
            categoryToSet = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
        }
        // Opción 2: categoryName explícito (más común desde frontend)
        else if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()
                && !dto.getCategoryName().equals("[object Object]")) {
            categoryToSet = categoryRepository.findByName(dto.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found with name: " + dto.getCategoryName()));
        }
        // Opción 3: Category object (si viene del frontend como objeto)
        else if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            categoryToSet = categoryRepository.findById(dto.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategory().getId()));
        }

        if (categoryToSet != null) {
            product.setCategory(categoryToSet);
        }

        if (dto.getTags() != null) product.setTags(dto.getTags());
    }
    
    /**
     * Actualiza las variantes de un producto
     * FIX: Evita ObjectDeletedException eliminando primero y luego procesando actualizaciones
     */
    private void updateProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
        // Si no hay variantes en el DTO, no hacer nada (mantener las existentes)
        if (variantDTOs == null || variantDTOs.isEmpty()) {
            return;
        }

        // PASO 1: Obtener variantes existentes de la base de datos
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
        System.out.println("📊 Variantes existentes en BD: " + existingVariants.size());
        existingVariants.forEach(v -> System.out.println("  - ID: " + v.getId() + ", SKU: " + v.getSku()));

        // PASO 2: Crear set de IDs que vienen en el DTO
        var incomingIds = variantDTOs.stream()
                .map(ProductVariantDTO::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
        System.out.println("📥 IDs en DTO recibido: " + incomingIds);

        // PASO 3: Identificar y ELIMINAR variantes que ya no están en el DTO
        // IMPORTANTE: Eliminar PRIMERO antes de actualizar para evitar conflictos de Hibernate
        List<ProductVariant> variantsToDelete = new java.util.ArrayList<>();
        for (ProductVariant existing : existingVariants) {
            if (!incomingIds.contains(existing.getId())) {
                variantsToDelete.add(existing);
            }
        }

        // Eliminar todas las variantes marcadas
        if (!variantsToDelete.isEmpty()) {
            System.out.println("🗑️ Eliminando " + variantsToDelete.size() + " variantes:");
            variantsToDelete.forEach(v -> System.out.println("  - ID: " + v.getId() + ", SKU: " + v.getSku() + ", Color: " + v.getColor()));

            productVariantRepository.deleteAll(variantsToDelete);
            productVariantRepository.flush(); // Forzar la eliminación AHORA

            System.out.println("✅ Variantes eliminadas y flush completado");
        } else {
            System.out.println("ℹ️ No hay variantes para eliminar");
        }

        // PASO 4: Actualizar o crear variantes del DTO
        // Re-obtener las variantes existentes después de eliminar
        List<ProductVariant> remainingVariants = productVariantRepository.findByProductId(product.getId());
        var remainingById = remainingVariants.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ProductVariant::getId,
                        v -> v,
                        (v1, v2) -> v1
                ));

        for (ProductVariantDTO variantDTO : variantDTOs) {
            ProductVariant variant = null;

            // Intentar match por ID primero (para variantes existentes)
            if (variantDTO.getId() != null && remainingById.containsKey(variantDTO.getId())) {
                variant = remainingById.get(variantDTO.getId());

                // Actualizar variante existente
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStock(variantDTO.getStock());
                variant.setPriceModifier(variantDTO.getPriceModifier());
                variant.setImageUrl(variantDTO.getImageUrl());
                variant.setAvailable(variantDTO.getAvailable() != null ? variantDTO.getAvailable() : true);
                // SKU no se actualiza para variantes existentes (inmutable)

                productVariantRepository.save(variant);
            } else {
                // Crear nueva variante (ID es null o no existe en remaining)
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