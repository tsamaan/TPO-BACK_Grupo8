package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    /**
     * Buscar variantes por producto
     */
    List<ProductVariant> findByProductId(String productId);
    
    /**
     * Buscar variantes disponibles por producto
     */
    List<ProductVariant> findByProductIdAndAvailableTrue(String productId);
    
    /**
     * Buscar variante por SKU
     */
    Optional<ProductVariant> findBySku(String sku);
    
    /**
     * Buscar variantes por producto y color
     */
    List<ProductVariant> findByProductIdAndColor(String productId, String color);
    
    /**
     * Buscar variantes con stock disponible
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stock > 0 AND v.available = true")
    List<ProductVariant> findAvailableVariantsWithStock(@Param("productId") String productId);
    
    /**
     * Verificar si existe una variante con el SKU dado
     */
    boolean existsBySku(String sku);
}
