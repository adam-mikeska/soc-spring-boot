package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Enums.DELIVERY;
import com.projekt.projekt.Models.Ecommerce.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository  extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByTitle(DELIVERY Title);
    Boolean existsByTitle(DELIVERY title);
}
