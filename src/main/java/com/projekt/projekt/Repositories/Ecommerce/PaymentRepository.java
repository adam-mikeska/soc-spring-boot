package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Enums.PAYMENT;
import com.projekt.projekt.Models.Ecommerce.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository  extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTitle(PAYMENT Title);
    Boolean existsByTitle(PAYMENT title);
}
