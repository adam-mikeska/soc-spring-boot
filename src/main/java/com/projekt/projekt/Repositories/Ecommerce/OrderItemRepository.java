package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Boolean existsByOrder_IdAndProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(Integer id,String title,String underTitle,String productSize);
    OrderItem findByOrder_IdAndProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(Integer id,String title,String underTitle,String productSize);
    Optional<OrderItem> findByIdAndOrder_Id(Integer id,Integer orderId);
}
