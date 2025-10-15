package com.tpo_api.haversack.service;

import com.tpo_api.haversack.dto.OrderDTO;
import com.tpo_api.haversack.model.Order;
import com.tpo_api.haversack.model.OrderItem;
import com.tpo_api.haversack.repository.OrderRepository;
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
        return orderRepository.findByStatusOrderByFechaDesc(status);
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
        order.setNombre(orderDTO.getNombre());
        order.setApellido(orderDTO.getApellido());
        order.setEmail(orderDTO.getEmail());
        order.setTelefono(orderDTO.getTelefono());
        order.setTotal(orderDTO.getTotal());
        order.setFecha(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        
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
        
        order.setProductos(orderItems);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
