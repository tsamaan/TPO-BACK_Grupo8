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
        loadDefaultCategories();
        loadDefaultProducts();
        
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
            
            

            User superAdmin = new User();
            superAdmin.setEmail("superadmin@haversack.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setNombre("Super");
            superAdmin.setApellido("Admin");
            superAdmin.setUsuario("superadmin");
            superAdmin.setName("Super Admin");
            superAdmin.setPhone("+54 11 1111-1111");

            // Configurar dirección embebida para superAdmin
            Direccion superAdminDireccion = new Direccion();
            superAdminDireccion.setCalle("Super Admin Address");
            superAdmin.setDireccion(superAdminDireccion);

            superAdmin.setRole(User.Role.SUPERADMIN);

            userRepository.save(superAdmin);

            log.info("Loaded default users");
        }
    }
    
    private void loadDefaultCategories() {
        if (categoryRepository.count() == 0) {
            Category mochilas = new Category();
            mochilas.setName("Mochilas");
            mochilas.setActive(true);
            mochilas.setDescription("Mochilas para todas tus aventuras");
            categoryRepository.save(mochilas);
            
            Category bolsos = new Category();
            bolsos.setName("Bolsos");
            bolsos.setActive(true);
            bolsos.setDescription("Bolsos versátiles y elegantes");
            categoryRepository.save(bolsos);
            
            Category materos = new Category();
            materos.setName("Materos");
            materos.setActive(true);
            materos.setDescription("El compañero perfecto para tu mate");
            categoryRepository.save(materos);
            
            Category accesorios = new Category();
            accesorios.setName("Accesorios");
            accesorios.setActive(true);
            accesorios.setDescription("Complementos para tu día a día");
            categoryRepository.save(accesorios);
            
            log.info("Loaded 4 default categories");
        }
    }
    
    private void loadDefaultProducts() {
        if (productRepository.count() == 0) {
            Category mochilas = categoryRepository.findByName("Mochilas").orElse(null);
            Category bolsos = categoryRepository.findByName("Bolsos").orElse(null);
            Category materos = categoryRepository.findByName("Materos").orElse(null);
            Category accesorios = categoryRepository.findByName("Accesorios").orElse(null);
            
            int productCount = 0;
            int variantCount = 0;
            
            // Producto 1: Mochila Urbana
            if (mochilas != null) {
                Product p1 = new Product();
                p1.setId("mochila-urbana-001");
                p1.setName("Mochila Urbana Pro");
                p1.setDescription("Mochila ideal para la ciudad con compartimento para laptop");
                p1.setPrice(25000.0);
                p1.setCategory(mochilas);
                p1.setImage("/img/Mochilas/mochila1.jpg");
                productRepository.save(p1);
                productCount++;
                
                // Variantes: Negro y Gris
                ProductVariant v1 = new ProductVariant();
                v1.setProduct(p1);
                v1.setSku("MOCHURB-001-BLK");
                v1.setColor("Negro");
                v1.setStock(15);
                v1.setAvailable(true);
                v1.setPriceModifier(0.0);
                v1.setImageUrl("/img/Mochilas/mochila1.jpg");
                productVariantRepository.save(v1);
                variantCount++;
                
                ProductVariant v2 = new ProductVariant();
                v2.setProduct(p1);
                v2.setSku("MOCHURB-001-GRY");
                v2.setColor("Gris");
                v2.setStock(10);
                v2.setAvailable(true);
                v2.setPriceModifier(0.0);
                v2.setImageUrl("/img/Mochilas/mochila1-gris.jpg");
                productVariantRepository.save(v2);
                variantCount++;
            }
            
            // Producto 2: Mochila Trekking
            if (mochilas != null) {
                Product p2 = new Product();
                p2.setId("mochila-trekking-002");
                p2.setName("Mochila Trekking Adventure");
                p2.setDescription("Mochila de gran capacidad para tus aventuras al aire libre");
                p2.setPrice(35000.0);
                p2.setCategory(mochilas);
                p2.setImage("/img/Mochilas/mochila2.jpg");
                productRepository.save(p2);
                productCount++;
                
                ProductVariant v3 = new ProductVariant();
                v3.setProduct(p2);
                v3.setSku("MOCHTREK-002-BLU");
                v3.setColor("Azul");
                v3.setStock(8);
                v3.setAvailable(true);
                v3.setPriceModifier(0.0);
                v3.setImageUrl("/img/Mochilas/mochila2.jpg");
                productVariantRepository.save(v3);
                variantCount++;
                
                ProductVariant v4 = new ProductVariant();
                v4.setProduct(p2);
                v4.setSku("MOCHTREK-002-GRN");
                v4.setColor("Verde");
                v4.setStock(12);
                v4.setAvailable(true);
                v4.setPriceModifier(0.0);
                v4.setImageUrl("/img/Mochilas/mochila2-verde.jpg");
                productVariantRepository.save(v4);
                variantCount++;
            }
            
            // Producto 3: Bolso Bandolera
            if (bolsos != null) {
                Product p3 = new Product();
                p3.setId("bolso-bandolera-003");
                p3.setName("Bolso Bandolera Classic");
                p3.setDescription("Bolso cruzado perfecto para el día a día");
                p3.setPrice(18000.0);
                p3.setCategory(bolsos);
                p3.setImage("/img/Bolsos/bolso1.jpg");
                productRepository.save(p3);
                productCount++;
                
                ProductVariant v5 = new ProductVariant();
                v5.setProduct(p3);
                v5.setSku("BOLBAND-003-BRN");
                v5.setColor("Marrón");
                v5.setStock(20);
                v5.setAvailable(true);
                v5.setPriceModifier(0.0);
                v5.setImageUrl("/img/Bolsos/bolso1.jpg");
                productVariantRepository.save(v5);
                variantCount++;
                
                ProductVariant v6 = new ProductVariant();
                v6.setProduct(p3);
                v6.setSku("BOLBAND-003-BLK");
                v6.setColor("Negro");
                v6.setStock(15);
                v6.setAvailable(true);
                v6.setPriceModifier(0.0);
                v6.setImageUrl("/img/Bolsos/bolso1-negro.jpg");
                productVariantRepository.save(v6);
                variantCount++;
            }
            
            // Producto 4: Matero Térmico
            if (materos != null) {
                Product p4 = new Product();
                p4.setId("matero-termico-004");
                p4.setName("Matero Térmico Premium");
                p4.setDescription("Mantiene tu termo a la temperatura perfecta");
                p4.setPrice(12000.0);
                p4.setCategory(materos);
                p4.setImage("/img/Materos/matero1.jpg");
                productRepository.save(p4);
                productCount++;
                
                ProductVariant v7 = new ProductVariant();
                v7.setProduct(p4);
                v7.setSku("MATER-004-RED");
                v7.setColor("Rojo");
                v7.setStock(25);
                v7.setAvailable(true);
                v7.setPriceModifier(0.0);
                v7.setImageUrl("/img/Materos/matero1.jpg");
                productVariantRepository.save(v7);
                variantCount++;
                
                ProductVariant v8 = new ProductVariant();
                v8.setProduct(p4);
                v8.setSku("MATER-004-BLU");
                v8.setColor("Azul");
                v8.setStock(20);
                v8.setAvailable(true);
                v8.setPriceModifier(0.0);
                v8.setImageUrl("/img/Materos/matero1-azul.jpg");
                productVariantRepository.save(v8);
                variantCount++;
            }
            
            log.info("Loaded {} default products with {} variants", productCount, variantCount);
        }
    }
}
