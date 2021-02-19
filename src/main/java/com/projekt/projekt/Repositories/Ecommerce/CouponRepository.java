package com.projekt.projekt.Repositories.Ecommerce;

import com.projekt.projekt.Models.Ecommerce.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Integer> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeAndEnabledIsTrue(String code);
    Boolean existsByCode(String code);
    @Query(value="SELECT c FROM Coupon c WHERE cast( c.id AS string ) like %:search% or c.code like %:search%")
    Page<Coupon> findAllBySearch(String search,Pageable pageable);
}
