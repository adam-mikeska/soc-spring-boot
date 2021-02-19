package com.projekt.projekt.Repositories.security;

import com.projekt.projekt.Models.security.TwoPhCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwoPhCodeRepository extends JpaRepository<TwoPhCode, Integer> {
    TwoPhCode findByEmail(String email);
    Boolean existsByEmail(String email);
}
