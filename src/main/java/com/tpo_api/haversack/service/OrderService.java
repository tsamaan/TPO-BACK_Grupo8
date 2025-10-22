package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.OrderDTO;
import com.tpo_api.haversack.model.Direccion;
import com.tpo_api.haversack.model.Order;
import com.tpo_api.haversack.model.OrderItem;
import com.tpo_api.haversack.repository.OrderRepository;
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
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByUsuario_EmailOrderByFechaDesc(email);
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
            userRepository.findByEmail(orderDTO.getEmail())
                    .ifPresent(order::setUsuario);
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
        order.setEstado(Order.OrderStatus.PENDING);
        
        // Crear items de la orden
        List<OrderItem> orderItems = orderDTO.getProductos().stream()
                .map(itemDTO -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemDTO.getId());
                    item.setName(itemDTO.getName());
                    item.setCantidad(itemDTO.getCantidad());
                    item.setPrecio(itemDTO.getPrecio());
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
