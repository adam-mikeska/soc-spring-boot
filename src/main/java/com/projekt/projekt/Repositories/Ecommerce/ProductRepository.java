package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Brand;
import com.projekt.projekt.Models.Ecommerce.Product;
import com.projekt.projekt.Models.Ecommerce.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository  extends JpaRepository<Product, Integer> {
    Boolean existsByTitle(String title);
    Product findByTitle(String title);
    List<Product> findAllByTitleContaining(String title);
    List<Product> findAllByBrand(Brand brand);
    List<Product> findAllByCategory_Id(Integer id);
    @Query(value = "SELECT * from product where category_id IN(WITH RECURSIVE cte (id) AS (SELECT id FROM category WHERE id =:parentId UNION SELECT t1.id FROM category t1 INNER JOIN cte t2 ON t1.parent_id = t2.id) SELECT * FROM cte)",nativeQuery=true)
    List<Product> findByCategory(Integer parentId);
}
