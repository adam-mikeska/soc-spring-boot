package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Cart_OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Cart_OrderDeliveryRepository  extends JpaRepository<Cart_OrderDelivery, Integer> {
    Boolean existsByCartId(String id);
    Cart_OrderDelivery findByCartId(String id);
}
