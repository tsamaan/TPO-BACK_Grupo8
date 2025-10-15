package com.tpo_api.haversack.repository;

import com.tpo_api.haversack.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
}
