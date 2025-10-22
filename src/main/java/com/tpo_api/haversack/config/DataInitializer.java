package com.tpo_api.haversack.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpo_api.haversack.model.Product;
import com.tpo_api.haversack.model.User;
import com.tpo_api.haversack.model.Category;
import com.tpo_api.haversack.repository.ProductRepository;
import com.tpo_api.haversack.repository.UserRepository;
import com.tpo_api.haversack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
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
            
            // Cargar productos
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
    private void loadProducts(Map<String, Object> data) {
        if (productRepository.count() == 0) {
            try {
                List<Map<String, Object>> productsData = (List<Map<String, Object>>) data.get("products");
                
                for (Map<String, Object> productData : productsData) {
                    Product product = objectMapper.convertValue(productData, Product.class);
                    productRepository.save(product);
                }
                
                log.info("Loaded {} products", productsData.size());
            } catch (Exception e) {
                log.error("Error loading products: ", e);
            }
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
                    user.setAddress((String) userData.get("address"));
                    user.setPhone((String) userData.get("phone"));
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
            admin.setAddress("Admin Address");
            admin.setPhone("+54 11 0000-0000");
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            
            User testUser = new User();
            testUser.setEmail("test@haversack.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setNombre("Test");
            testUser.setApellido("User");
            testUser.setUsuario("testuser");
            testUser.setName("Test User");
            testUser.setAddress("Test Address");
            testUser.setPhone("+54 11 0000-0000");
            testUser.setRole(User.Role.USER);
            
            userRepository.save(testUser);
            
            log.info("Loaded default users");
        }
    }
}
