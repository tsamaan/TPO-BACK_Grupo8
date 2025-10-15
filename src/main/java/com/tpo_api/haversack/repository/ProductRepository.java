package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t IN :tags")
    List<Product> findByTagsIn(@Param("tags") List<String> tags);
    
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByCategoryAndPriceBetween(@Param("category") String category, 
                                               @Param("minPrice") Double minPrice, 
                                               @Param("maxPrice") Double maxPrice);
}
