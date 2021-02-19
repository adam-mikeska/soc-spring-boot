package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.ProductOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository  extends JpaRepository<ProductOption, Integer> {
    @Query(value="SELECT c FROM ProductOption c LEFT JOIN c.product p LEFT JOIN p.brand b WHERE  cast( c.id AS string ) like %:search% or CONCAT(COALESCE(b.name,''),' ',p.title,' ',c.underTitle) like %:search%")
    Page<ProductOption> findAllBySearch(String search,Pageable pageable);
    @Query(value="SELECT c FROM ProductOption c LEFT JOIN c.product p LEFT JOIN p.brand b WHERE  CONCAT(COALESCE(b.name,''),' ',p.title,' ',c.underTitle) like %:search% AND (:priceMin is null or c.price-c.discount>=:priceMin) AND (:priceMax is null or c.price-c.discount<=:priceMax)  AND c.enabled = true")
    Page<ProductOption> findAllProductsByKeyWord(String search,BigDecimal priceMin, BigDecimal priceMax,Pageable pageable);
    List<ProductOption> findAllByProduct_CategoryIdAndEnabledIsTrue(Integer id);
    List<ProductOption> findAllByProduct_Category_Name(String name);
    Optional<List<ProductOption>> findAllByProduct_Brand_Name(String name);
    Optional<List<ProductOption>> findAllByProduct_Title(String title);
    @Query(value="SELECT * FROM product_option WHERE enabled = true ORDER BY sold_times DESC Limit 0, 4", nativeQuery=true)
    List<ProductOption> findTop4ByOrderBySoldTimes();
    @Query(value="SELECT * FROM product_option WHERE enabled = true ORDER BY added_in_date DESC Limit 0, 4", nativeQuery=true)
    List<ProductOption> findTop4ByOrderByAddedInDateDesc();
    @Query(value="SELECT * FROM product_option WHERE enabled = true ORDER BY (product_option.price-(product_option.price-product_option.discount))/(product_option.price-product_option.discount)*10 DESC Limit 0, 4", nativeQuery=true)
    List<ProductOption> findTop4ByOrderByDiscountDesc();
    List<ProductOption> findAllByProduct_TitleAndEnabledIsTrue(String title);
    Boolean existsByProduct_Title(String title);
    Boolean existsByProduct_TitleAndUnderTitleAndEnabledIsTrue(String title,String underTitle);
    Boolean existsByProduct_TitleAndUnderTitle(String title,String underTitle);
    Optional<ProductOption> findByProduct_TitleAndUnderTitleAndEnabledIsTrue(String title,String underTitle);
    Optional<ProductOption> findByProduct_TitleAndUnderTitle(String title,String underTitle);
    @Query(value = "SELECT * FROM product_option p WHERE  (:priceMin is null or p.price-p.discount>=:priceMin)  AND (:priceMax is null or p.price-p.discount<=:priceMax) AND p.enabled is true AND p.product_id IN(SELECT id from product where category_id IN(WITH RECURSIVE cte (id) AS (SELECT id FROM category WHERE id =:parentId UNION SELECT t1.id FROM category t1 INNER JOIN cte t2 ON t1.parent_id = t2.id) SELECT * FROM cte))",nativeQuery=true)
    Page<ProductOption> findByCategory(Integer parentId, BigDecimal priceMin, BigDecimal priceMax,Pageable pageable);
    @Query(value = "SELECT * FROM product_option p WHERE  p.enabled is true AND p.product_id IN(SELECT id from product where category_id IN(WITH RECURSIVE cte (id) AS (SELECT id FROM category WHERE id =:parentId UNION SELECT t1.id FROM category t1 INNER JOIN cte t2 ON t1.parent_id = t2.id) SELECT * FROM cte))",nativeQuery=true)
    Optional<List<ProductOption>> findByCategory(Integer parentId);

}
