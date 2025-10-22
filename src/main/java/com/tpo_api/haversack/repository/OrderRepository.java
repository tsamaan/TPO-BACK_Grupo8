package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUsuario_EmailOrderByFechaDesc(String email);
    
    List<Order> findByEstadoOrderByFechaDesc(Order.OrderStatus estado);
    
    @Query("SELECT o FROM Order o WHERE o.fecha BETWEEN :startDate AND :endDate ORDER BY o.fecha DESC")
    List<Order> findByFechaBetween(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.fecha BETWEEN :startDate AND :endDate")
    Double getTotalSalesBetween(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
}
