package com.proyecto.ecommerce.repository;

import com.proyecto.ecommerce.model.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDetalleOrdenRepository extends JpaRepository<DetalleOrden, Integer> {
    boolean existsByProducto_Id(Integer productoId);
}
