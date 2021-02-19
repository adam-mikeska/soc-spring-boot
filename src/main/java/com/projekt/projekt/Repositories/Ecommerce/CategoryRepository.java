package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Category;
import com.projekt.projekt.Models.Ecommerce.ProductOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByParent(Category parent);
    List<Category> findAllByNameContaining(String name);
    Boolean existsByParent_IdAndName(Integer parrentId,String name);
    Boolean existsByNameAndParentIsNull(String name);
    List<Category> findAllByParent_Id(Integer id);
    @Query(value="WITH RECURSIVE cte (id,name,parent_id) AS (SELECT id,name,parent_id FROM category WHERE id =:parentid  UNION SELECT t1.id ,t1.name,t1.parent_id FROM category t1 INNER JOIN cte t2 ON t1.parent_id = t2.id ) SELECT * FROM cte",nativeQuery=true)
    Optional<List<Category>> findChildCategories(Integer parentid);
    @Query(value="SELECT c FROM Category c WHERE CONCAT(c.name) like %:search% ")
    Page<Category> autocompleteCategories(String search, Pageable pageable);
}
