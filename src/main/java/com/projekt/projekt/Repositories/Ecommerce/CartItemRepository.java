package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CartItemRepository  extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(String title, String underTitle, String productSize, String cartId);
    Boolean existsCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(String title,String underTitle,String productSize,String cartId);
    List<CartItem> findAllByCartId(String cartId);
    List<CartItem> findAllByProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(String title,String underTitle,String productSize);
    List<CartItem> findAllByProductOption_UnderTitle(String undertitle);
}
