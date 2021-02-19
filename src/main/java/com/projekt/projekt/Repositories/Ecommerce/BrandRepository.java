package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Page<Brand> findAllByNameContaining(String name, Pageable pageable);
    List<Brand> findAllByNameContaining(String name);
    Boolean existsByName(String name);
    Brand findByName(String name);
}
