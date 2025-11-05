package com.tpo_api.haversack.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpo_api.haversack.model.Product;
import com.tpo_api.haversack.model.ProductVariant;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.model.Category;
import com.tpo_api.haversack.model.Direccion;
import com.tpo_api.haversack.repository.ProductRepository;
import com.tpo_api.haversack.repository.ProductVariantRepository;
import com.tpo_api.haversack.repository.UserRepository;
import com.tpo_api.haversack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }
    
    private void loadInitialData() {
        try {
            // Cargar datos desde data.json
            ClassPathResource resource = new ClassPathResource("data.json");
            InputStream inputStream = resource.getInputStream();
            
            Map<String, Object> data = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
            
            // Cargar categorías primero
            loadCategories(data);
            
            // Cargar productos (después de las categorías)
            loadProducts(data);
            
            // Cargar usuarios
            loadUsers(data);
            
            log.info("Initial data loaded successfully");
            
        } catch (Exception e) {
            log.error("Error loading initial data: ", e);
            loadDefaultData();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadCategories(Map<String, Object> data) {
        if (categoryRepository.count() == 0) {
            try {
                List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("products");
                
                // Extraer categorías únicas de los productos
                Set<String> uniqueCategories = new HashSet<>();
                for (Map<String, Object> productData : productsData) {
                    String categoryName = (String) productData.get("category");
                    if (categoryName != null) {
                        uniqueCategories.add(categoryName);
                    }
                }
                
                // Crear entidades Category para cada categoría única
                for (String categoryName : uniqueCategories) {
                    Category category = new Category();
                    category.setName(categoryName);
                    category.setActive(true);
                    categoryRepository.save(category);
                }
                
                log.info("Loaded {} categories", uniqueCategories.size());
            } catch (Exception e) {
                log.error("Error loading categories: ", e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadProducts(Map<String, Object> data) {
        // Siempre limpiar y recargar los productos para testing
        log.info("Clearing existing products and variants...");
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        
        try {
            List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("products");
            int variantCount = 0;
                
                for (Map<String, Object> productData : productsData) {
                    // Extraer campos necesarios
                    String categoryName = (String) productData.get("category");
                    List<String> colores = (List<String>) productData.get("colores");
                    Object stockData = productData.get("stock");
                    
                    // Convertir stock a Integer
                    Integer totalStock = 0;
                    if (stockData instanceof String) {
                        try {
                            totalStock = Integer.parseInt((String) stockData);
                        } catch (NumberFormatException e) {
                            totalStock = 10; // Stock por defecto
                        }
                    } else if (stockData instanceof Integer) {
                        totalStock = (Integer) stockData;
                    }
                    
                    // Extraer los datos necesarios ANTES de convertir
                    List<String> imagesList = (List<String>) productData.get("images");
                    if (imagesList == null) {
                        imagesList = new ArrayList<>();
                    }
                    
                    List<String> tagsList = (List<String>) productData.get("tags");
                    if (tagsList == null) {
                        tagsList = new ArrayList<>();
                    }
                    
                    // Remover campos que no pertenecen a Product
                    Map<String, Object> productDataWithoutCategory = new java.util.HashMap<>(productData);
                    productDataWithoutCategory.remove("category");
                    productDataWithoutCategory.remove("colores");
                    productDataWithoutCategory.remove("stock");
                    productDataWithoutCategory.remove("quantity");
                    productDataWithoutCategory.remove("images"); // Lo asignamos manualmente después
                    productDataWithoutCategory.remove("tags"); // Lo asignamos manualmente después
                    
                    // Si no hay "image" pero sí hay "images", usar la primera imagen del array
                    if (productDataWithoutCategory.get("image") == null && !imagesList.isEmpty()) {
                        productDataWithoutCategory.put("image", imagesList.get(0));
                    }
                    
                    // Convertir el producto
                    Product product = objectMapper.convertValue(productDataWithoutCategory, Product.class);
                    
                    // Asignar manualmente las listas @ElementCollection
                    product.setImages(new ArrayList<>(imagesList));
                    product.setTags(new ArrayList<>(tagsList));
                    
                    // Buscar y asignar la categoría
                    if (categoryName != null) {
                        Category category = categoryRepository.findByName(categoryName)
                                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
                        product.setCategory(category);
                    }
                    
                    // Guardar el producto primero
                    product = productRepository.save(product);
                    
                    // Crear variantes basadas en los colores
                    if (colores != null && !colores.isEmpty()) {
                        int stockPorColor = totalStock / colores.size(); // Distribuir stock uniformemente
                        int stockExtra = totalStock % colores.size(); // Stock sobrante
                        
                        // Obtener las imágenes disponibles
                        List<String> availableImages = product.getImages();
                        String mainImage = product.getImage();
                        
                        for (int i = 0; i < colores.size(); i++) {
                            String color = colores.get(i);
                            ProductVariant variant = new ProductVariant();
                            variant.setProduct(product);
                            variant.setSku(product.getId() + "-" + color.toUpperCase().replaceAll("\\s+", "-"));
                            variant.setColor(color);
                            variant.setStock(stockPorColor + (i == 0 ? stockExtra : 0)); // Dar el extra al primer color
                            variant.setAvailable(true);
                            variant.setPriceModifier(0.0); // Sin modificador de precio
                            
                            // Asignar imagen: si existe en el array images[i], usarla; sino usar la imagen principal
                            String variantImage = mainImage; // Por defecto usa la imagen principal
                            if (availableImages != null && !availableImages.isEmpty()) {
                                // Si hay imágenes en el array, intentar usar una por cada color
                                if (i < availableImages.size()) {
                                    variantImage = availableImages.get(i);
                                } else {
                                    // Si hay más colores que imágenes, reutilizar las imágenes disponibles
                                    variantImage = availableImages.get(i % availableImages.size());
                                }
                            }
                            
                            variant.setImageUrl(variantImage);
                            
                            productVariantRepository.save(variant);
                            variantCount++;
                        }
                    } else {
                        // Si no hay colores, crear una variante por defecto
                        ProductVariant defaultVariant = new ProductVariant();
                        defaultVariant.setProduct(product);
                        defaultVariant.setSku(product.getId() + "-DEFAULT");
                        defaultVariant.setColor("Default");
                        defaultVariant.setStock(totalStock);
                        defaultVariant.setAvailable(true);
                        defaultVariant.setPriceModifier(0.0);
                        defaultVariant.setImageUrl(product.getImage());
                        
                        productVariantRepository.save(defaultVariant);
                        variantCount++;
                    }
                }
                
                log.info("Loaded {} products with {} variants", productsData.size(), variantCount);
            } catch (Exception e) {
                log.error("Error loading products: ", e);
            }
    }
    
    @SuppressWarnings("unchecked")
    private void loadUsers(Map<String, Object> data) {
        if (userRepository.count() == 0) {
            try {
                List<Map<String, Object>> usersData = (List<Map<String, Object>>) data.get("users");
                
                for (Map<String, Object> userData : usersData) {
                    User user = new User();
                    user.setEmail((String) userData.get("email"));
                    user.setPassword(passwordEncoder.encode((String) userData.get("password")));
                    user.setNombre((String) userData.get("nombre"));
                    user.setApellido((String) userData.get("apellido"));
                    user.setUsuario((String) userData.get("usuario"));
                    user.setName((String) userData.get("name"));
                    user.setPhone((String) userData.get("phone"));
                    
                    // Configurar dirección embebida
                    String addressString = (String) userData.get("address");
                    if (addressString != null) {
                        Direccion direccion = new Direccion();
                        direccion.setCalle(addressString);
                        user.setDireccion(direccion);
                    }
                    
                    user.setRole(User.Role.USER);
                    
                    userRepository.save(user);
                }
                
                log.info("Loaded {} users", usersData.size());
            } catch (Exception e) {
                log.error("Error loading users: ", e);
            }
        }
    }
    
    private void loadDefaultData() {
        // Cargar datos por defecto si falla la carga desde JSON
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@haversack.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Admin");
            admin.setApellido("User");
            admin.setUsuario("admin");
            admin.setName("Admin User");
            admin.setPhone("+54 11 0000-0000");
            
            // Configurar dirección embebida para admin
            Direccion adminDireccion = new Direccion();
            adminDireccion.setCalle("Admin Address");
            admin.setDireccion(adminDireccion);
            
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            
            User testUser = new User();
            testUser.setEmail("test@haversack.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setNombre("Test");
            testUser.setApellido("User");
            testUser.setUsuario("testuser");
            testUser.setName("Test User");
            testUser.setPhone("+54 11 0000-0000");
            
            // Configurar dirección embebida para testUser
            Direccion testDireccion = new Direccion();
            testDireccion.setCalle("Test Address");
            testUser.setDireccion(testDireccion);
            
            testUser.setRole(User.Role.USER);
            
            userRepository.save(testUser);
            
            log.info("Loaded default users");
        }
    }
}
