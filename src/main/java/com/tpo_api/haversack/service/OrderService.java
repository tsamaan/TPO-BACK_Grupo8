package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.OrderDTO;
import com.tpo_api.haversack.exception.BadRequestException;
import com.tpo_api.haversack.exception.NotFoundException;
import com.tpo_api.haversack.model.Direccion;
import com.tpo_api.haversack.model.Order;
import com.tpo_api.haversack.model.OrderItem;
import com.tpo_api.haversack.model.ProductVariant;
import com.tpo_api.haversack.repository.OrderRepository;
import com.tpo_api.haversack.repository.ProductVariantRepository;
import com.tpo_api.haversack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByEmailOrderByFechaDesc(email);
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByEstadoOrderByFechaDesc(status);
    }
    
    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByFechaBetween(startDate, endDate);
    }
    
    public Double getTotalSalesBetween(LocalDateTime startDate, LocalDateTime endDate) {
        Double total = orderRepository.getTotalSalesBetween(startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        
        // Buscar usuario por email si se proporciona
        if (orderDTO.getEmail() != null) {
            var userOptional = userRepository.findByEmail(orderDTO.getEmail());
            if (userOptional.isPresent()) {
                order.setUsuario(userOptional.get());
            } else {
                // Si no hay usuario, guardar como orden de invitado
                order.setGuestName(orderDTO.getNombre() + " " + orderDTO.getApellido());
                order.setGuestEmail(orderDTO.getEmail());
                order.setGuestPhone(orderDTO.getTelefono());
            }
        }
        
        // Configurar dirección embebida
        Direccion direccion = new Direccion();
        // Aquí podrías extraer la dirección del DTO o usar datos del usuario
        if (orderDTO.getNombre() != null && orderDTO.getApellido() != null) {
            direccion.setCalle(orderDTO.getNombre() + " " + orderDTO.getApellido());
        }
        order.setDireccion(direccion);
        
        order.setTotal(orderDTO.getTotal());
        order.setFecha(LocalDateTime.now());
        order.setEstado(Order.OrderStatus.CONFIRMED); // Cambiado a CONFIRMED para reflejar orden completada
        
        // Crear items de la orden y reducir stock de las variantes
        List<OrderItem> orderItems = orderDTO.getProductos().stream()
                .map(itemDTO -> {
                    // Reducir stock de la variante si existe
                    if (itemDTO.getVariantId() != null) {
                        ProductVariant variant = productVariantRepository.findById(itemDTO.getVariantId())
                                .orElseThrow(() -> new NotFoundException(
                                    "Product variant not found with ID: " + itemDTO.getVariantId() + 
                                    ". Please refresh the page and try again."));
                        
                        // Verificar que hay suficiente stock
                        if (variant.getStock() < itemDTO.getCantidad()) {
                            throw new BadRequestException("Insufficient stock for " + itemDTO.getName() + 
                                    " (Color: " + variant.getColor() + "). Available: " + variant.getStock() + 
                                    ", Requested: " + itemDTO.getCantidad());
                        }
                        
                        // Reducir el stock
                        variant.setStock(variant.getStock() - itemDTO.getCantidad());
                        productVariantRepository.save(variant);
                    }
                    
                    OrderItem item = new OrderItem();
                    item.setProductId(itemDTO.getId());
                    item.setName(itemDTO.getName());
                    item.setCantidad(itemDTO.getCantidad());
                    item.setPrecio(itemDTO.getPrecio());
                    item.setVariantId(itemDTO.getVariantId());
                    item.setSku(itemDTO.getSku());
                    item.setColor(itemDTO.getColor());
                    item.setSize(itemDTO.getSize());
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());
        
        order.setDetallesPedido(orderItems);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setEstado(status);
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
