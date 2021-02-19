package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Enums.OrderState;
import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query(value="SELECT c FROM Order c WHERE c.name like %:search% or cast( c.id AS string ) like %:search% or c.email like %:search% or c.telNumber like %:search%")
    Page<Order> findAllByNameContainingOrEmailContainingOrEmailContainingOrTelNumberContaining(String search, Pageable pageable);
    Page<Order> findAllByUser_Id(Integer id,Pageable pageable);
    Page<Order> findAllByUser_Email(String email,Pageable pageable);
    Optional<Order> findByIdAndUser_Email(Integer id, String email);
    Boolean existsByIdAndUser_Email(Integer id,String email);

    @Query("SELECT b FROM Order b WHERE EXTRACT (day FROM b.created) = :day AND EXTRACT (month FROM b.created) = :month  AND EXTRACT (year FROM b.created) = :year")
    List<Order> findAllByCreatedDay(Integer day,Integer month, Integer year);
    List<Order> findAllByOrderState(OrderState orderState);
}
