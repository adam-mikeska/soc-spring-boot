package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {
    Optional<ProductSize> findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(String size, String title, String underTitle);
    Boolean existsBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(String size, String title, String underTitle);
}
