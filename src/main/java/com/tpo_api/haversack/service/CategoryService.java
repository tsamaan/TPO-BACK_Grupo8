package com.tpo_api.haversack.service;

import com.tpo_api.haversack.model.Category;
import com.tpo_api.haversack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Verificar que el nombre no exista en otra categoría
        if (!category.getName().equals(categoryDetails.getName()) && 
            categoryRepository.existsByName(categoryDetails.getName())) {
            throw new RuntimeException("Category with name '" + categoryDetails.getName() + "' already exists");
        }
        
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setImage(categoryDetails.getImage());
        category.setActive(categoryDetails.getActive());
        
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
