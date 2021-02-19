package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Cart_OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Cart_OrderPaymentRepository extends JpaRepository<Cart_OrderPayment,Integer> {
    Cart_OrderPayment findByCartId(String id);
    Boolean existsByCartId(String id);
}
